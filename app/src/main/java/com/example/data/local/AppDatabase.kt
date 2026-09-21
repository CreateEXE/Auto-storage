package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FileMetadata
import com.example.data.model.RenamingRuleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FileMetadata::class, RenamingRuleEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun renamingRuleDao(): RenamingRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "file_organizer_database.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Prepopulate default rules
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.renamingRuleDao()?.insertRules(
                                    RenamingRuleEntity.getDefaultRules()
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
