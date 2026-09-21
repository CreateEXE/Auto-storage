package com.example.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.model.FileMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import java.util.UUID

data class Sha256WorkerSummary(
    val totalFiles: Int = 0,
    val hashesComputed: Int = 0,
    val duplicatesFound: Int = 0,
    val reclaimableBytes: Long = 0L,
    val elapsedMillis: Long = 0L,
    val statusMessage: String = ""
) {
    val formattedSavings: String
        get() = FileMetadata.formatFileSize(reclaimableBytes)

    val formattedDuration: String
        get() = "%.1f s".format(Locale.US, elapsedMillis / 1000.0)
}

data class Sha256WorkerState(
    val isRunning: Boolean = false,
    val isEnqueued: Boolean = false,
    val progressPercent: Int = 0,
    val currentFilename: String = "",
    val processedCount: Int = 0,
    val totalCount: Int = 0,
    val duplicatesFound: Int = 0,
    val reclaimableBytes: Long = 0L,
    val summary: Sha256WorkerSummary? = null,
    val errorMessage: String? = null
)

object Sha256WorkerScheduler {
    const val UNIQUE_WORK_NAME = "sha256_duplicate_scan_worker"
    const val TAG_SHA256_WORKER = "tag_sha256_duplicate_worker"

    const val KEY_FORCE_REHASH = "key_force_rehash"
    const val KEY_PROGRESS_PERCENT = "key_progress_percent"
    const val KEY_CURRENT_FILENAME = "key_current_filename"
    const val KEY_PROCESSED_COUNT = "key_processed_count"
    const val KEY_TOTAL_COUNT = "key_total_count"
    const val KEY_DUPLICATES_COUNT = "key_duplicates_count"
    const val KEY_RECLAIMABLE_BYTES = "key_reclaimable_bytes"
    const val KEY_HASHES_COMPUTED = "key_hashes_computed"
    const val KEY_TOTAL_FILES = "key_total_files"
    const val KEY_ELAPSED_MILLIS = "key_elapsed_millis"
    const val KEY_STATUS_MESSAGE = "key_status_message"

    fun enqueueSha256DuplicateWorker(
        context: Context,
        forceRehash: Boolean = false
    ): UUID {
        val inputData = workDataOf(
            KEY_FORCE_REHASH to forceRehash
        )

        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<Sha256DuplicateWorker>()
            .addTag(TAG_SHA256_WORKER)
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        return workRequest.id
    }

    fun cancelSha256DuplicateWorker(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    fun getWorkInfoFlow(context: Context): Flow<WorkInfo?> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
            .map { list -> list.firstOrNull() }
    }

    fun observeWorkerState(context: Context): Flow<Sha256WorkerState> {
        return getWorkInfoFlow(context).map { workInfo ->
            if (workInfo == null) {
                return@map Sha256WorkerState()
            }

            val isRunning = workInfo.state == WorkInfo.State.RUNNING
            val isEnqueued = workInfo.state == WorkInfo.State.ENQUEUED
            val isSucceeded = workInfo.state == WorkInfo.State.SUCCEEDED
            val isFailed = workInfo.state == WorkInfo.State.FAILED
            val isCancelled = workInfo.state == WorkInfo.State.CANCELLED

            val progressData = workInfo.progress
            val outputData = workInfo.outputData

            val percent = progressData.getInt(KEY_PROGRESS_PERCENT, 0)
            val currentFile = progressData.getString(KEY_CURRENT_FILENAME) ?: ""
            val processed = progressData.getInt(KEY_PROCESSED_COUNT, 0)
            val total = progressData.getInt(KEY_TOTAL_COUNT, 0)
            val duplicates = progressData.getInt(KEY_DUPLICATES_COUNT, 0)
            val reclaimable = progressData.getLong(KEY_RECLAIMABLE_BYTES, 0L)

            val summary = if (isSucceeded) {
                Sha256WorkerSummary(
                    totalFiles = outputData.getInt(KEY_TOTAL_FILES, total),
                    hashesComputed = outputData.getInt(KEY_HASHES_COMPUTED, processed),
                    duplicatesFound = outputData.getInt(KEY_DUPLICATES_COUNT, duplicates),
                    reclaimableBytes = outputData.getLong(KEY_RECLAIMABLE_BYTES, reclaimable),
                    elapsedMillis = outputData.getLong(KEY_ELAPSED_MILLIS, 0L),
                    statusMessage = outputData.getString(KEY_STATUS_MESSAGE) ?: "Duplicate scan completed."
                )
            } else null

            val error = if (isFailed) {
                outputData.getString(KEY_STATUS_MESSAGE) ?: "Worker execution failed."
            } else if (isCancelled) {
                "SHA-256 worker cancelled by user."
            } else null

            Sha256WorkerState(
                isRunning = isRunning,
                isEnqueued = isEnqueued,
                progressPercent = if (isSucceeded) 100 else percent,
                currentFilename = if (isRunning) currentFile else "",
                processedCount = processed,
                totalCount = total,
                duplicatesFound = duplicates,
                reclaimableBytes = reclaimable,
                summary = summary,
                errorMessage = error
            )
        }
    }
}
