package com.smarteyex.memory

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.*
import java.util.UUID

/* =========================
   MEMORY TYPES
========================= */

enum class MemoryType {
    CORE_IDENTITY,
    EMOTIONAL,
    SOCIAL,
    TEMPORAL
}

/* =========================
   ENTITY
========================= */

@Entity(tableName = "memory_store")
data class MemoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: MemoryType,
    val summary: String,          // MAKNA, bukan log
    val emotion: String?,          // sedih, senang, capek, kosong
    val relatedPerson: String?,    // Zahra, Ibu, dll
    val timestamp: Long,
    val importance: Int            // 1–10 (buat pelupaan)
)

/* =========================
   DAO
========================= */

@Dao
interface MemoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: MemoryEntity)

    @Query("SELECT * FROM memory_store WHERE importance >= :minImportance ORDER BY timestamp DESC")
    suspend fun getImportantMemories(minImportance: Int = 5): List<MemoryEntity>

    @Query("SELECT * FROM memory_store WHERE relatedPerson = :name")
    suspend fun getMemoriesByPerson(name: String): List<MemoryEntity>

    @Query("DELETE FROM memory_store WHERE importance <= :threshold")
    suspend fun forgetLowImportance(threshold: Int = 2)

    @Query("DELETE FROM memory_store")
    suspend fun wipeAll()
}

/* =========================
   DATABASE
========================= */

@Database(entities = [MemoryEntity::class], version = 1)
abstract class MemoryDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile private var INSTANCE: MemoryDatabase? = null

        fun get(context: Context): MemoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MemoryDatabase::class.java,
                    "smart_memory.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

/* =========================
   MEMORY ENGINE (OTAK)
========================= */

class SmartMemoryEngine(context: Context) {

    private val dao = MemoryDatabase.get(context).memoryDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /* === SIMPAN MAKNA === */
    fun remember(
        type: MemoryType,
        summary: String,
        emotion: String? = null,
        relatedPerson: String? = null,
        importance: Int = 5
    ) {
        scope.launch {
            dao.insert(
                MemoryEntity(
                    type = type,
                    summary = summary,
                    emotion = emotion,
                    relatedPerson = relatedPerson,
                    timestamp = System.currentTimeMillis(),
                    importance = importance
                )
            )
        }
    }

    /* === DIPANGGIL SAAT USER DIAM / CURHAT === */
    suspend fun getEmotionalContext(): String? {
        val memories = dao.getImportantMemories()
        return memories.firstOrNull { it.type == MemoryType.EMOTIONAL }?.summary
    }

    /* === KENAL ORANG === */
    suspend fun getPersonContext(name: String): String? {
        return dao.getMemoriesByPerson(name)
            .maxByOrNull { it.importance }
            ?.summary
    }
memoryEngine.remember(
    type = MemoryType.SOCIAL,
    summary = "User ngobrol topik politik dengan temannya",
    importance = 4
)

    /* === LUPA KAYAK MANUSIA === */
    fun decayMemory() {
        scope.launch {
            dao.forgetLowImportance()
        }
    }

    /* === MODE DARURAT / RESET === */
    fun wipeMemory() {
        scope.launch {
            dao.wipeAll()
        }
    }
}