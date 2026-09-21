package com.example.data.local

import com.example.data.model.FileMetadata
import com.example.data.model.RenamingRuleEntity
import kotlinx.coroutines.flow.Flow

class FileRepository(
    private val fileDao: FileDao,
    private val renamingRuleDao: RenamingRuleDao
) {
    val allFiles: Flow<List<FileMetadata>> = fileDao.getAllFiles()
    val duplicateFiles: Flow<List<FileMetadata>> = fileDao.getDuplicates()
    val audioFiles: Flow<List<FileMetadata>> = fileDao.getAudioFiles()
    val allRules: Flow<List<RenamingRuleEntity>> = renamingRuleDao.getAllRules()
    val activeRules: Flow<List<RenamingRuleEntity>> = renamingRuleDao.getActiveRules()

    fun getFilesByCategory(category: String): Flow<List<FileMetadata>> =
        fileDao.getFilesByCategory(category)

    fun getImagesBySubtype(subtype: String): Flow<List<FileMetadata>> =
        fileDao.getImagesBySubtype(subtype)

    fun searchFiles(query: String): Flow<List<FileMetadata>> =
        fileDao.searchFiles(query)

    suspend fun getAllFilesList(): List<FileMetadata> =
        fileDao.getAllFilesList()

    suspend fun getDuplicatesList(): List<FileMetadata> =
        fileDao.getDuplicatesList()

    suspend fun getFilesByHash(hash: String): List<FileMetadata> =
        fileDao.getFilesByHash(hash)

    suspend fun getFileById(id: Long): FileMetadata? =
        fileDao.getFileById(id)

    suspend fun insertFiles(files: List<FileMetadata>) =
        fileDao.insertFiles(files)

    suspend fun insertFile(file: FileMetadata) =
        fileDao.insertFile(file)

    suspend fun updateFile(file: FileMetadata) =
        fileDao.updateFile(file)

    suspend fun updateFiles(files: List<FileMetadata>) =
        fileDao.updateFiles(files)

    suspend fun deleteFile(file: FileMetadata) =
        fileDao.deleteFile(file)

    suspend fun deleteFiles(files: List<FileMetadata>) =
        fileDao.deleteFiles(files)

    suspend fun deleteFilesByIds(ids: List<Long>) =
        fileDao.deleteFilesByIds(ids)

    suspend fun clearAll() =
        fileDao.clearAll()

    // Renaming Rules methods
    suspend fun insertRule(rule: RenamingRuleEntity): Long =
        renamingRuleDao.insertRule(rule)

    suspend fun updateRule(rule: RenamingRuleEntity) =
        renamingRuleDao.updateRule(rule)

    suspend fun deleteRule(rule: RenamingRuleEntity) =
        renamingRuleDao.deleteRule(rule)

    suspend fun deleteRuleById(id: Long) =
        renamingRuleDao.deleteRuleById(id)
}
