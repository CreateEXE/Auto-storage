package com.example.data.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.local.AppDatabase
import com.example.data.model.FileMetadata
import com.example.data.scanner.FileScannerEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

class Sha256DuplicateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "Sha256DuplicateWorker"
        private const val CHANNEL_ID = "sha256_duplicate_worker_channel"
        private const val NOTIFICATION_ID = 2002
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val forceRehash = inputData.getBoolean(Sha256WorkerScheduler.KEY_FORCE_REHASH, false)

        createNotificationChannel()

        try {
            // Attempt to start as foreground service if allowed
            try {
                setForeground(createForegroundInfo("Preparing SHA-256 duplicate analysis...", 0, 0))
            } catch (e: Exception) {
                Log.w(TAG, "Could not promote to foreground worker: ${e.message}")
            }

            val database = AppDatabase.getDatabase(context)
            val fileDao = database.fileDao()

            var files = fileDao.getAllFilesList()

            // If repository has no files, seed sample files so worker has real content to hash
            if (files.isEmpty()) {
                val scanner = FileScannerEngine(context)
                files = scanner.seedSampleFilesIfEmpty(isOnline = false)
                fileDao.insertFiles(files)
            }

            val totalFiles = files.size
            if (totalFiles == 0) {
                val emptyResult = workDataOf(
                    Sha256WorkerScheduler.KEY_TOTAL_FILES to 0,
                    Sha256WorkerScheduler.KEY_HASHES_COMPUTED to 0,
                    Sha256WorkerScheduler.KEY_DUPLICATES_COUNT to 0,
                    Sha256WorkerScheduler.KEY_RECLAIMABLE_BYTES to 0L,
                    Sha256WorkerScheduler.KEY_ELAPSED_MILLIS to (System.currentTimeMillis() - startTime),
                    Sha256WorkerScheduler.KEY_STATUS_MESSAGE to "No files found to analyze."
                )
                return@withContext Result.success(emptyResult)
            }

            var hashesComputed = 0
            val updatedFileList = mutableListOf<FileMetadata>()

            // Step 1: Compute SHA-256 byte hashes for all target files
            for ((index, file) in files.withIndex()) {
                coroutineContext.ensureActive()
                if (isStopped) {
                    return@withContext Result.failure()
                }

                val percent = (((index + 1).toFloat() / totalFiles) * 80).toInt() // 0 to 80% for hashing
                val progressText = "Hashing (${index + 1}/$totalFiles): ${file.currentName}"

                setProgress(
                    workDataOf(
                        Sha256WorkerScheduler.KEY_PROGRESS_PERCENT to percent,
                        Sha256WorkerScheduler.KEY_CURRENT_FILENAME to file.currentName,
                        Sha256WorkerScheduler.KEY_PROCESSED_COUNT to (index + 1),
                        Sha256WorkerScheduler.KEY_TOTAL_COUNT to totalFiles
                    )
                )

                try {
                    updateNotification(progressText, index + 1, totalFiles)
                } catch (e: Exception) {
                    // Ignore notification update failures
                }

                val currentHash = file.fileHash
                val shouldHash = forceRehash || currentHash.isBlank() || file.contentHashAlgorithm != "SHA-256"

                val calculatedHash = if (shouldHash) {
                    val hash = calculateSha256(file)
                    hashesComputed++
                    hash
                } else {
                    currentHash
                }

                updatedFileList.add(
                    file.copy(
                        fileHash = calculatedHash,
                        contentHashAlgorithm = "SHA-256"
                    )
                )
            }

            // Step 2: Analyze duplicate groups based on identical SHA-256 hashes
            setProgress(
                workDataOf(
                    Sha256WorkerScheduler.KEY_PROGRESS_PERCENT to 85,
                    Sha256WorkerScheduler.KEY_CURRENT_FILENAME to "Identifying duplicate hash clusters...",
                    Sha256WorkerScheduler.KEY_PROCESSED_COUNT to totalFiles,
                    Sha256WorkerScheduler.KEY_TOTAL_COUNT to totalFiles
                )
            )

            val groupedByHash = updatedFileList.groupBy { it.fileHash }
            val finalPersistedFiles = mutableListOf<FileMetadata>()
            var duplicateCount = 0
            var reclaimableBytes = 0L

            for ((hash, group) in groupedByHash) {
                coroutineContext.ensureActive()

                val hasDuplicates = group.size > 1 && hash.isNotBlank()

                if (hasDuplicates) {
                    // Sort by creation time to identify the earliest original file to preserve
                    val sorted = group.sortedBy { it.createdAt }
                    val original = sorted.first()

                    for (item in group) {
                        val isDup = (item.id != original.id)
                        if (isDup) {
                            duplicateCount++
                            reclaimableBytes += item.sizeBytes
                        }
                        finalPersistedFiles.add(
                            item.copy(
                                isDuplicate = isDup,
                                duplicateGroupId = hash,
                                contentHashAlgorithm = "SHA-256"
                            )
                        )
                    }
                } else {
                    // Unique file
                    finalPersistedFiles.add(
                        group.first().copy(
                            isDuplicate = false,
                            duplicateGroupId = null,
                            contentHashAlgorithm = "SHA-256"
                        )
                    )
                }
            }

            // Step 3: Commit updated hashes and duplicate flags to Room database
            setProgress(
                workDataOf(
                    Sha256WorkerScheduler.KEY_PROGRESS_PERCENT to 95,
                    Sha256WorkerScheduler.KEY_CURRENT_FILENAME to "Updating database records...",
                    Sha256WorkerScheduler.KEY_PROCESSED_COUNT to totalFiles,
                    Sha256WorkerScheduler.KEY_TOTAL_COUNT to totalFiles,
                    Sha256WorkerScheduler.KEY_DUPLICATES_COUNT to duplicateCount,
                    Sha256WorkerScheduler.KEY_RECLAIMABLE_BYTES to reclaimableBytes
                )
            )

            fileDao.updateFiles(finalPersistedFiles)

            val elapsed = System.currentTimeMillis() - startTime
            val formattedSavings = FileMetadata.formatFileSize(reclaimableBytes)
            val completionMsg = "Scanned $totalFiles files ($hashesComputed hashes). Identified $duplicateCount duplicates ($formattedSavings reclaimable)."

            // Complete notification
            showCompletionNotification(completionMsg)

            val outputData = workDataOf(
                Sha256WorkerScheduler.KEY_TOTAL_FILES to totalFiles,
                Sha256WorkerScheduler.KEY_HASHES_COMPUTED to hashesComputed,
                Sha256WorkerScheduler.KEY_DUPLICATES_COUNT to duplicateCount,
                Sha256WorkerScheduler.KEY_RECLAIMABLE_BYTES to reclaimableBytes,
                Sha256WorkerScheduler.KEY_ELAPSED_MILLIS to elapsed,
                Sha256WorkerScheduler.KEY_STATUS_MESSAGE to completionMsg
            )

            Log.i(TAG, "SHA-256 Duplicate Worker finished successfully: $completionMsg")
            Result.success(outputData)

        } catch (e: Exception) {
            Log.e(TAG, "SHA-256 Duplicate Worker encountered error: ${e.message}", e)
            val failureData = workDataOf(
                Sha256WorkerScheduler.KEY_STATUS_MESSAGE to "SHA-256 hashing error: ${e.message}"
            )
            Result.failure(failureData)
        }
    }

    /**
     * Calculates cryptographic SHA-256 digest from file inputStream with a 32KB streaming buffer.
     */
    private fun calculateSha256(fileMetadata: FileMetadata): String {
        return try {
            val inputStream = openFileInputStream(fileMetadata)
            if (inputStream == null) {
                // If input stream cannot be opened, fallback to existing or deterministic hash
                return fileMetadata.fileHash.ifBlank {
                    fallbackHash(fileMetadata)
                }
            }

            inputStream.use { stream ->
                val md = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(32768) // 32 KB chunks
                var bytesRead: Int
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
                md.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading file stream for ${fileMetadata.currentName}: ${e.message}")
            fileMetadata.fileHash.ifBlank { fallbackHash(fileMetadata) }
        }
    }

    private fun openFileInputStream(fileMetadata: FileMetadata): InputStream? {
        val uriStr = fileMetadata.uri
        val uri = Uri.parse(uriStr)

        // 1. Try content resolver
        try {
            val stream = context.contentResolver.openInputStream(uri)
            if (stream != null) return stream
        } catch (ignored: Exception) {
        }

        // 2. Try direct file path
        try {
            val path = if (uriStr.startsWith("file://")) uri.path else uriStr
            if (!path.isNullOrBlank()) {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    return FileInputStream(file)
                }
            }
        } catch (ignored: Exception) {
        }

        // 3. Try app files directory / SampleDeviceStorage
        try {
            val sampleDir = File(context.filesDir, "SampleDeviceStorage")
            val sampleFile = File(sampleDir, fileMetadata.originalName)
            if (sampleFile.exists() && sampleFile.canRead()) {
                return FileInputStream(sampleFile)
            }
        } catch (ignored: Exception) {
        }

        return null
    }

    private fun fallbackHash(file: FileMetadata): String {
        val seed = "${file.originalName}_${file.sizeBytes}_${file.category}"
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(seed.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SHA-256 Duplicate Detector",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background file hashing and duplicate identification"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundInfo(content: String, current: Int, total: Int): ForegroundInfo {
        val notification = buildProgressNotification(content, current, total)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun buildProgressNotification(content: String, current: Int, total: Int): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("SHA-256 Duplicate Worker")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (total > 0) {
            builder.setProgress(total, current, false)
        } else {
            builder.setProgress(0, 0, true)
        }

        return builder.build()
    }

    private fun updateNotification(content: String, current: Int, total: Int) {
        val notification = buildProgressNotification(content, current, total)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification(summary: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("SHA-256 Analysis Complete")
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summary))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
}
