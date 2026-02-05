package com.smarteyex.memory

import android.content.Context
import androidx.room.*
import com.smarteyex.core.AppState
import com.smarteyex.core.voice.VoiceEngine
import com.smarteyex.notification.WhatsAppReplySender
import kotlinx.coroutines.*
import java.util.UUID

/* =========================
   MEMORY TYPES
========================= */

enum class MemoryType {
    CORE_IDENTITY,
    EMOTIONAL,
    SOCIAL,
    TEMPORAL,
    WA_MESSAGE,
    VOICE_REPLY
}

/* =========================
   ENTITY DATABASE
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
   MEMORY ENGINE (FINAL)
========================= */

class SmartMemoryEngine(private val context: Context) {

    private val dao = MemoryDatabase.get(context).memoryDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /* =========================
       SIMPAN MAKNA UTAMA
    ========================== */
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

    /* =========================
       INGAT EMOSI PENGGUNA
    ========================== */
    suspend fun getEmotionalContext(): String? {
        val memories = dao.getImportantMemories()
        return memories.firstOrNull { it.type == MemoryType.EMOTIONAL }?.summary
    }

    /* =========================
       INGAT ORANG / KONTEKS SOSIAL
    ========================== */
    suspend fun getPersonContext(name: String): String? {
        return dao.getMemoriesByPerson(name)
            .maxByOrNull { it.importance }
            ?.summary
    }

    /* =========================
       LUPA KAYAK MANUSIA
    ========================== */
    fun decayMemory() {
        scope.launch {
            dao.forgetLowImportance()
        }
    }

    /* =========================
       RESET MEMORI
    ========================== */
    fun wipeMemory() {
        scope.launch {
            dao.wipeAll()
            ShortMemoryStore.clear()
        }
    }

    /* =========================
       WA MESSAGE → MEMORY + VOICE INTEGRATION
    ========================== */
    fun onWaMessageReceived(sender: String, message: String) {
        remember(
            type = MemoryType.WA_MESSAGE,
            summary = message,
            relatedPerson = sender,
            importance = if (AppState.isPriorityContact(sender)) 10 else 5
        )

        if (!AppState.isBusy.get()) {
            VoiceEngine.speak(
                "Ada pesan dari $sender: ${message.take(80)}"
            )
        }
    }

    fun onUserVoiceReply(reply: String, targetSbn: android.service.notification.StatusBarNotification?) {
        remember(
            type = MemoryType.VOICE_REPLY,
            summary = reply,
            importance = 7
        )

        targetSbn?.let {
            WhatsAppReplySender.sendReply(it, reply)
        }
    }

    /* =========================
       REAL-TIME SHORT MEMORY
    ========================== */
    fun addShortMemory(topic: String, intent: String, emotion: String) {
        ShortMemoryStore.add(topic, intent, emotion)
    }

    fun recentShortMemories(): List<ShortMemory> = ShortMemoryStore.recent()
}

/* =========================
   SHORT MEMORY
========================= */

data class ShortMemory(
    val topic: String,
    val intent: String,
    val emotion: String,
    val timestamp: Long
)

object ShortMemoryStore {
    private val memories = mutableListOf<ShortMemory>()

    fun add(topic: String, intent: String, emotion: String) {
        memories.add(
            ShortMemory(topic, intent, emotion, System.currentTimeMillis())
        )
        if (memories.size > 20) memories.removeAt(0) // real-time micro memory
    }

    fun recent(): List<ShortMemory> = memories
    fun clear() = memories.clear()
}