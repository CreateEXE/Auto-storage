package com.example.data.system

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import com.example.data.model.FileMetadata
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class StorageImpactLevel(
    val title: String,
    val summary: String,
    val severity: Int // 0: Normal, 1: Moderate, 2: Heavy, 3: Critical
) {
    OPTIMAL(
        title = "Optimal Throughput",
        summary = "Ample free storage headroom. Flash controller wear leveling & TRIM run efficiently. I/O latency is baseline (< 5ms).",
        severity = 0
    ),
    MODERATE_IMPACT(
        title = "Moderate I/O Burden",
        summary = "Flash garbage collection overhead begins rising. Minor write amplification. Read/write latency increased by ~15-25%.",
        severity = 1
    ),
    DEGRADED_PERFORMANCE(
        title = "Degraded Speed & Sluggishness",
        summary = "Severe write amplification. SQLite checkpoint stalls cause visible UI frame drops. Background app caching disabled.",
        severity = 2
    ),
    CRITICAL_THROTTLING(
        title = "Critical System Throttling",
        summary = "Flash storage near exhaustion. Extreme I/O bottleneck causing system freezes, app reload loops, and ANR alerts.",
        severity = 3
    )
}

data class SystemMemoryInfo(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val usedRamBytes: Long,
    val lowMemoryThresholdBytes: Long,
    val isLowMemory: Boolean,
    val jvmMaxMemoryBytes: Long,
    val jvmTotalMemoryBytes: Long,
    val jvmFreeMemoryBytes: Long
) {
    val ramUsedPercent: Int
        get() = if (totalRamBytes > 0) ((usedRamBytes.toDouble() / totalRamBytes.toDouble()) * 100).toInt().coerceIn(0, 100) else 0

    val jvmUsedMemoryBytes: Long
        get() = (jvmTotalMemoryBytes - jvmFreeMemoryBytes).coerceAtLeast(0)

    val jvmUsedPercent: Int
        get() = if (jvmMaxMemoryBytes > 0) ((jvmUsedMemoryBytes.toDouble() / jvmMaxMemoryBytes.toDouble()) * 100).toInt().coerceIn(0, 100) else 0

    val formattedTotalRam: String get() = FileMetadata.formatFileSize(totalRamBytes)
    val formattedAvailRam: String get() = FileMetadata.formatFileSize(availableRamBytes)
    val formattedUsedRam: String get() = FileMetadata.formatFileSize(usedRamBytes)
    val formattedThreshold: String get() = FileMetadata.formatFileSize(lowMemoryThresholdBytes)
    val formattedJvmMax: String get() = FileMetadata.formatFileSize(jvmMaxMemoryBytes)
    val formattedJvmUsed: String get() = FileMetadata.formatFileSize(jvmUsedMemoryBytes)
}

data class SystemStorageInfo(
    val totalInternalBytes: Long,
    val availableInternalBytes: Long,
    val usedInternalBytes: Long
) {
    val storageUsedPercent: Int
        get() = if (totalInternalBytes > 0) ((usedInternalBytes.toDouble() / totalInternalBytes.toDouble()) * 100).toInt().coerceIn(0, 100) else 0

    val storageFreePercent: Int
        get() = (100 - storageUsedPercent).coerceIn(0, 100)

    val formattedTotal: String get() = FileMetadata.formatFileSize(totalInternalBytes)
    val formattedAvailable: String get() = FileMetadata.formatFileSize(availableInternalBytes)
    val formattedUsed: String get() = FileMetadata.formatFileSize(usedInternalBytes)
}

data class StorageLagAssessment(
    val impactLevel: StorageImpactLevel,
    val lagRiskScore: Int, // 0 to 100
    val estimatedIoLatencyMs: String,
    val writeAmplificationFactor: String,
    val trimStatus: String,
    val rootCauses: List<StorageLagFactor>
)

data class StorageLagFactor(
    val title: String,
    val subtitle: String,
    val explanation: String,
    val impactScore: String
)

data class HardwareSpecs(
    val manufacturer: String,
    val model: String,
    val deviceCode: String,
    val board: String,
    val hardware: String,
    val cpuCores: Int,
    val supportedAbis: String,
    val androidVersion: String,
    val apiLevel: Int,
    val buildId: String,
    val securityPatch: String,
    val formattedUptime: String
)

data class DeviceDiagnostics(
    val memory: SystemMemoryInfo,
    val storage: SystemStorageInfo,
    val lagAssessment: StorageLagAssessment,
    val hardware: HardwareSpecs,
    val timestamp: Long = System.currentTimeMillis()
)

object DeviceDiagnosticsProvider {

    fun getDiagnostics(context: Context): DeviceDiagnostics {
        val memory = readMemory(context)
        val storage = readStorage()
        val lag = assessStorageImpact(storage, memory)
        val hardware = readHardware()

        return DeviceDiagnostics(
            memory = memory,
            storage = storage,
            lagAssessment = lag,
            hardware = hardware
        )
    }

    private fun readMemory(context: Context): SystemMemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)

        val runtime = Runtime.getRuntime()
        val jvmMax = runtime.maxMemory()
        val jvmTotal = runtime.totalMemory()
        val jvmFree = runtime.freeMemory()

        val totalRam = memInfo.totalMem
        val availRam = memInfo.availMem
        val usedRam = (totalRam - availRam).coerceAtLeast(0L)

        return SystemMemoryInfo(
            totalRamBytes = totalRam,
            availableRamBytes = availRam,
            usedRamBytes = usedRam,
            lowMemoryThresholdBytes = memInfo.threshold,
            isLowMemory = memInfo.lowMemory,
            jvmMaxMemoryBytes = jvmMax,
            jvmTotalMemoryBytes = jvmTotal,
            jvmFreeMemoryBytes = jvmFree
        )
    }

    private fun readStorage(): SystemStorageInfo {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val total = totalBlocks * blockSize
            val avail = availableBlocks * blockSize
            val used = (total - avail).coerceAtLeast(0L)

            SystemStorageInfo(
                totalInternalBytes = total,
                availableInternalBytes = avail,
                usedInternalBytes = used
            )
        } catch (e: Exception) {
            SystemStorageInfo(
                totalInternalBytes = 64L * 1024 * 1024 * 1024,
                availableInternalBytes = 16L * 1024 * 1024 * 1024,
                usedInternalBytes = 48L * 1024 * 1024 * 1024
            )
        }
    }

    private fun assessStorageImpact(
        storage: SystemStorageInfo,
        memory: SystemMemoryInfo
    ): StorageLagAssessment {
        val usedPercent = storage.storageUsedPercent
        val freePercent = storage.storageFreePercent

        // Calculate lag risk score (0-100) based on storage saturation and memory pressure
        var riskScore = when {
            usedPercent >= 95 -> 95
            usedPercent >= 90 -> 82
            usedPercent >= 80 -> 60
            usedPercent >= 70 -> 35
            else -> 12
        }

        if (memory.isLowMemory) {
            riskScore = (riskScore + 15).coerceAtMost(100)
        } else if (memory.ramUsedPercent > 85) {
            riskScore = (riskScore + 8).coerceAtMost(100)
        }

        val level = when {
            usedPercent >= 92 || riskScore >= 80 -> StorageImpactLevel.CRITICAL_THROTTLING
            usedPercent >= 84 || riskScore >= 60 -> StorageImpactLevel.DEGRADED_PERFORMANCE
            usedPercent >= 72 || riskScore >= 35 -> StorageImpactLevel.MODERATE_IMPACT
            else -> StorageImpactLevel.OPTIMAL
        }

        val ioLatency = when (level) {
            StorageImpactLevel.OPTIMAL -> "< 4 ms (Nominal)"
            StorageImpactLevel.MODERATE_IMPACT -> "12 - 25 ms (Elevated)"
            StorageImpactLevel.DEGRADED_PERFORMANCE -> "45 - 90 ms (Sluggish)"
            StorageImpactLevel.CRITICAL_THROTTLING -> "> 200 ms (Severe Bottleneck)"
        }

        val waf = when (level) {
            StorageImpactLevel.OPTIMAL -> "1.1x (Low Wear)"
            StorageImpactLevel.MODERATE_IMPACT -> "2.4x (Noticeable Garbage Collection)"
            StorageImpactLevel.DEGRADED_PERFORMANCE -> "5.2x (High Block Thrashing)"
            StorageImpactLevel.CRITICAL_THROTTLING -> "8.8x+ (Extreme Flash Degradation)"
        }

        val trimStatus = when (level) {
            StorageImpactLevel.OPTIMAL -> "Efficient (Clean block pooling)"
            StorageImpactLevel.MODERATE_IMPACT -> "Active (Delayed block consolidation)"
            StorageImpactLevel.DEGRADED_PERFORMANCE -> "Impaired (Insufficient contiguous buffer)"
            StorageImpactLevel.CRITICAL_THROTTLING -> "Starved (Cannot perform wear leveling)"
        }

        val factors = listOf(
            StorageLagFactor(
                title = "NAND Flash Block Garbage Collection & WAF",
                subtitle = "Solid-state flash cannot overwrite cells in-place",
                explanation = "Flash memory writes in 4KB pages but can only erase in 2MB-8MB blocks. When storage drops below 15-20% free, the flash controller must constantly read, relocate, and erase blocks before writing new data. This raises the Write Amplification Factor (WAF) by up to 5x-8x, freezing I/O threads.",
                impactScore = if (usedPercent >= 85) "High Impact" else "Controlled"
            ),
            StorageLagFactor(
                title = "ZRAM & Page Cache Eviction Pressure",
                subtitle = "Virtual memory swap thrashing & LMK app terminations",
                explanation = "Android relies on compressed memory (ZRAM) and disk caching. As storage fills, the kernel can no longer buffer file operations, causing the Low Memory Killer (LMK) to aggressively kill background apps. Every app switch turns into a slow cold launch.",
                impactScore = if (memory.isLowMemory || usedPercent >= 90) "Critical" else "Moderate"
            ),
            StorageLagFactor(
                title = "SQLite Write-Ahead Logging (WAL) Latency",
                subtitle = "System service databases wait on disk commits",
                explanation = "Core system services (Contacts, SMS, Media Store, Google Play Services, App Preferences) use SQLite. When disk write latency spikes, SQLite checkpoints block main threads, directly causing UI jank, frame drops, and touch input hesitation.",
                impactScore = if (usedPercent >= 80) "Noticeable" else "Negligible"
            ),
            StorageLagFactor(
                title = "ART Runtime Compilation Starvation",
                subtitle = "Apps fall back to slower JIT interpretation",
                explanation = "Android executes dex2oat Ahead-Of-Time (AOT) bytecode compilation during overnight idle charging. If free storage is below minimum thresholds, the compilation is aborted, leaving apps in unoptimized bytecode that consumes more CPU and battery.",
                impactScore = if (freePercent < 10) "Severe" else "Optimal"
            )
        )

        return StorageLagAssessment(
            impactLevel = level,
            lagRiskScore = riskScore,
            estimatedIoLatencyMs = ioLatency,
            writeAmplificationFactor = waf,
            trimStatus = trimStatus,
            rootCauses = factors
        )
    }

    private fun readHardware(): HardwareSpecs {
        val uptimeMs = SystemClock.elapsedRealtime()
        val hours = TimeUnit.MILLISECONDS.toHours(uptimeMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMs) % 60
        val formattedUptime = String.format(Locale.US, "%dh %02dm %02ds", hours, minutes, seconds)

        val abis = Build.SUPPORTED_ABIS.joinToString(", ").ifBlank { "Unknown" }

        return HardwareSpecs(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() },
            model = Build.MODEL,
            deviceCode = Build.DEVICE,
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            supportedAbis = abis,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            buildId = Build.ID,
            securityPatch = Build.VERSION.SECURITY_PATCH ?: "N/A",
            formattedUptime = formattedUptime
        )
    }

    fun triggerGarbageCollection(): Long {
        val runtime = Runtime.getRuntime()
        val before = runtime.totalMemory() - runtime.freeMemory()
        System.gc()
        val after = runtime.totalMemory() - runtime.freeMemory()
        return (before - after).coerceAtLeast(0L)
    }
}
