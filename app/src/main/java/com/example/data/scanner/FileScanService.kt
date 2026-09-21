package com.example.data.scanner

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.data.ai.GeminiFileAnalyzer
import com.example.data.local.AppDatabase
import com.example.data.local.FileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FileScanService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var repository: FileRepository
    private lateinit var scannerEngine: FileScannerEngine

    companion object {
        private const val CHANNEL_ID = "file_scan_channel"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START_SCAN = "com.example.action.START_SCAN"
        const val ACTION_STOP_SCAN = "com.example.action.STOP_SCAN"
        const val EXTRA_SCAN_URI = "com.example.extra.SCAN_URI"

        private val _isScanning = MutableStateFlow(false)
        val isScanning = _isScanning.asStateFlow()

        private val _progressText = MutableStateFlow("")
        val progressText = _progressText.asStateFlow()
        
        private val _scannedCount = MutableStateFlow(0)
        val scannedCount = _scannedCount.asStateFlow()

        fun startScan(context: Context, treeUri: Uri? = null) {
            val intent = Intent(context, FileScanService::class.java).apply {
                action = ACTION_START_SCAN
                putExtra(EXTRA_SCAN_URI, treeUri?.toString())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopScan(context: Context) {
            val intent = Intent(context, FileScanService::class.java).apply {
                action = ACTION_STOP_SCAN
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(applicationContext)
        repository = FileRepository(database.fileDao(), database.renamingRuleDao())
        scannerEngine = FileScannerEngine(applicationContext, GeminiFileAnalyzer())
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SCAN -> {
                val uriString = intent.getStringExtra(EXTRA_SCAN_URI)
                val treeUri = uriString?.let { Uri.parse(it) }
                startForeground(NOTIFICATION_ID, createNotification("Starting file scan..."))
                performScan(treeUri)
            }
            ACTION_STOP_SCAN -> {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun performScan(treeUri: Uri?) {
        if (_isScanning.value) return
        _isScanning.value = true
        _scannedCount.value = 0

        serviceScope.launch {
            try {
                val scannedFiles = if (treeUri != null) {
                    scannerEngine.scanDocumentTree(treeUri) { fileName, count ->
                        _progressText.value = "Hashing #$count: $fileName"
                        _scannedCount.value = count
                        updateNotification("Hashing #$count: $fileName")
                    }
                } else {
                    scannerEngine.scanDeviceMediaStore { fileName, count ->
                        _progressText.value = "Hashing #$count: $fileName"
                        _scannedCount.value = count
                        updateNotification("Hashing #$count: $fileName")
                    }
                }
                
                repository.insertFiles(scannedFiles)
                _progressText.value = "Scan complete. ${scannedFiles.size} files indexed."
            } catch (e: Exception) {
                _progressText.value = "Scan failed: ${e.message}"
            } finally {
                _isScanning.value = false
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "File Scan Service"
            val descriptionText = "Notifications for background file scanning and hashing"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("File Content Analysis")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        _isScanning.value = false
    }
}
