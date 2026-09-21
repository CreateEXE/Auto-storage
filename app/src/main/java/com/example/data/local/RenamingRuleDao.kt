package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RenamingRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RenamingRuleDao {
    @Query("SELECT * FROM renaming_rules ORDER BY priority ASC, id ASC")
    fun getAllRules(): Flow<List<RenamingRuleEntity>>

    @Query("SELECT * FROM renaming_rules WHERE isActive = 1 ORDER BY priority ASC, id ASC")
    fun getActiveRules(): Flow<List<RenamingRuleEntity>>

    @Query("SELECT * FROM renaming_rules WHERE id = :id LIMIT 1")
    suspend fun getRuleById(id: Long): RenamingRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RenamingRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<RenamingRuleEntity>)

    @Update
    suspend fun updateRule(rule: RenamingRuleEntity)

    @Delete
    suspend fun deleteRule(rule: RenamingRuleEntity)

    @Query("DELETE FROM renaming_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)

    @Query("SELECT COUNT(*) FROM renaming_rules")
    suspend fun getRuleCount(): Int
}
