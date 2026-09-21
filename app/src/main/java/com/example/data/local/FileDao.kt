package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FileMetadata
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM scanned_files ORDER BY modifiedAt DESC")
    fun getAllFiles(): Flow<List<FileMetadata>>

    @Query("SELECT * FROM scanned_files WHERE category = :category ORDER BY modifiedAt DESC")
    fun getFilesByCategory(category: String): Flow<List<FileMetadata>>

    @Query("SELECT * FROM scanned_files WHERE category = 'AUDIO' ORDER BY artist ASC, album ASC, trackNumber ASC, currentName ASC")
    fun getAudioFiles(): Flow<List<FileMetadata>>

    @Query("SELECT * FROM scanned_files WHERE category = 'IMAGES' AND imageSubtype = :subtype ORDER BY modifiedAt DESC")
    fun getImagesBySubtype(subtype: String): Flow<List<FileMetadata>>

    @Query("SELECT * FROM scanned_files WHERE isDuplicate = 1 ORDER BY sizeBytes DESC")
    fun getDuplicates(): Flow<List<FileMetadata>>

    @Query("SELECT * FROM scanned_files WHERE isDuplicate = 1 OR duplicateGroupId IS NOT NULL ORDER BY fileHash ASC, createdAt ASC")
    fun getAllDuplicateCandidates(): Flow<List<FileMetadata>>

    @Query("SELECT * FROM scanned_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Long): FileMetadata?

    @Query("SELECT * FROM scanned_files ORDER BY modifiedAt DESC")
    suspend fun getAllFilesList(): List<FileMetadata>

    @Query("SELECT * FROM scanned_files WHERE isDuplicate = 1 ORDER BY sizeBytes DESC")
    suspend fun getDuplicatesList(): List<FileMetadata>

    @Query("SELECT * FROM scanned_files WHERE fileHash = :hash")
    suspend fun getFilesByHash(hash: String): List<FileMetadata>

    @Query("""
        SELECT * FROM scanned_files 
        WHERE currentName LIKE '%' || :query || '%' 
           OR originalName LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%' 
           OR extractedSummary LIKE '%' || :query || '%'
           OR artist LIKE '%' || :query || '%'
           OR album LIKE '%' || :query || '%'
           OR trackTitle LIKE '%' || :query || '%'
           OR genre LIKE '%' || :query || '%'
           OR imageSubtype LIKE '%' || :query || '%'
        ORDER BY modifiedAt DESC
    """)
    fun searchFiles(query: String): Flow<List<FileMetadata>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileMetadata>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileMetadata): Long

    @Update
    suspend fun updateFile(file: FileMetadata)

    @Update
    suspend fun updateFiles(files: List<FileMetadata>)

    @Delete
    suspend fun deleteFile(file: FileMetadata)

    @Delete
    suspend fun deleteFiles(files: List<FileMetadata>)

    @Query("DELETE FROM scanned_files WHERE id IN (:ids)")
    suspend fun deleteFilesByIds(ids: List<Long>)

    @Query("DELETE FROM scanned_files")
    suspend fun clearAll()
}
