package com.smarteyex.core.memory

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.smarteyex.core.R
import com.smarteyex.core.BaseNeonActivity
import com.smarteyex.core.NeonTouchLayer
import com.smarteyex.core.state.AppState

class MemoryActivity : BaseNeonActivity() {

    private lateinit var memoryContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory)

        val rootLayout = findViewById<LinearLayout>(R.id.memoryRoot)
        val touchLayer = NeonTouchLayer(this, NeonTouchLayer.EffectType.MEMORY_PARTICLE)
        rootLayout.addView(touchLayer)

        memoryContainer = findViewById(R.id.memoryContainer)
        scrollView = findViewById(R.id.memoryScroll)

        loadMemoryTimeline()
    }

    private fun loadMemoryTimeline() {
        // Ambil memori dari MemoryManager
        val memories = AppState.memoryList
        for (memory in memories) {
            addMemoryItem(memory)
        }
    }

    private fun addMemoryItem(memory: MemoryManager.MemoryItem) {
        val inflater = LayoutInflater.from(this)
        val itemView = inflater.inflate(R.layout.memory_item, memoryContainer, false) as TextView
        itemView.text = "${memory.date} - ${memory.label}: ${memory.detail}"
        // Neon glow / animasi partikel bisa diaktifkan via XML drawable / animasi
        memoryContainer.addView(itemView)

        itemView.setOnClickListener {
            // Tap effect + opsi hapus / edit / mark done
            // Bisa disinkron ke AppState.memoryList
            handleMemoryItemTap(memory)
        }
    }

    private fun handleMemoryItemTap(memory: MemoryManager.MemoryItem) {
        // Contoh: hapus atau update reminder
        AppState.memoryList.remove(memory)
        memoryContainer.removeAllViews()
        loadMemoryTimeline()
    }

    fun addNewMemory(label: String, detail: String) {
        val newMemory = MemoryManager.MemoryItem(label, detail)
        AppState.memoryList.add(newMemory)
        addMemoryItem(newMemory)
    }
}