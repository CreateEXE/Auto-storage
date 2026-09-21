package com.example.data.model

data class DuplicateGroup(
    val groupId: String,
    val fileHash: String,
    val originalFile: FileMetadata,
    val duplicateFiles: List<FileMetadata>,
    val totalSavingsBytes: Long = duplicateFiles.sumOf { it.sizeBytes }
) {
    val formattedSavings: String
        get() = FileMetadata.formatFileSize(totalSavingsBytes)
}

data class StorageStats(
    val totalFiles: Int = 0,
    val totalBytes: Long = 0L,
    val duplicateCount: Int = 0,
    val duplicateSavingsBytes: Long = 0L,
    val renamedCount: Int = 0,
    val pendingRenameCount: Int = 0,
    val organizedCount: Int = 0,
    val taggedCount: Int = 0,
    val categorySizes: Map<FileCategory, Long> = emptyMap()
) {
    val formattedTotalSize: String
        get() = FileMetadata.formatFileSize(totalBytes)

    val formattedDuplicateSavings: String
        get() = FileMetadata.formatFileSize(duplicateSavingsBytes)
}
