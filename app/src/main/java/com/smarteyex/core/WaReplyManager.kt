package com.smarteyex.core.wa

import com.smarteyex.core.AppSpeak

var waMode:Boolean = false

data class WaChat(
    val sender:String,
    val msg:String,
    val isGroup:Boolean,
    val group:String?
)

object WaReplyManager {

    private val queue = mutableListOf<WaChat>()
    private val silentQueue = mutableListOf<WaChat>()

    private var selected = -1
    private var waitSelect = false
    private var waitReply = false

    var isBusy = false

    // ===============================
    // ENTRY VOICE
    // ===============================

    fun queueVoice(text:String){
        if(text.isBlank()) return
        if(!waMode) return
        onVoice(text)
    }

    // ===============================
    // INCOMING MESSAGE
    // ===============================

    fun onIncoming(sender:String,msg:String,isGroup:Boolean,group:String?) {

        if(isBusy){
            queueSilent(sender,msg,isGroup,group)
            return
        }

        waMode = true
        isBusy = true

        queue.add(WaChat(sender,msg,isGroup,group))

        if(queue.size > 1){
            waitSelect = true
            waitReply = false
            AppSpeak.speak("Ada ${queue.size} pesan. Mau balas nomor berapa?")
        }else{
            selected = 0
            waitSelect = false
            waitReply = true
            speakChat(queue[0])
        }
    }

    // ===============================
    // VOICE PROCESS
    // ===============================

    private fun onVoice(spoken:String){

        val cmd = spoken.lowercase()

        if(waitSelect){
            val i = parseNumber(cmd)
            if(i >= 0 && i < queue.size){
                selected = i
                waitSelect = false
                waitReply = true
                speakChat(queue[i])
            }
            return
        }

        if(waitReply){

            when {

                cmd.contains("baca") || cmd.contains("read") -> {
                    clear()
                }

                cmd.contains("tunggu") -> {
                    DelayManager.set(spoken)
                    clear()
                }

                cmd.contains("jawab") || cmd.contains("balas") -> {
                    send(
                        cmd
                            .replace("jawab","")
                            .replace("balas","")
                            .trim()
                    )
                }

                else -> {
                    send(spoken)
                }
            }
        }
    }

    // ===============================
    // SPEAK CHAT
    // ===============================

    private fun speakChat(c:WaChat){

        if(c.isGroup)
            AppSpeak.speak("Grup ${c.group ?: "tidak dikenal"}. ${c.sender} bilang ${c.msg}")
        else
            AppSpeak.speak("Dari ${c.sender}. ${c.msg}")
    }

    // ===============================
    // SEND MESSAGE
    // ===============================

    private fun send(text:String){
        if(selected >= 0){
            WaSender.send(text)
        }
        clear()
        flushSilent()
    }

    // ===============================
    // CLEAR STATE
    // ===============================

    private fun clear(){
        queue.clear()
        selected = -1
        waitSelect = false
        waitReply = false
        waMode = false
        isBusy = false
    }

    // ===============================
    // SILENT QUEUE
    // ===============================

    fun queueSilent(s:String,m:String,g:Boolean,gr:String?){
        silentQueue.add(WaChat(s,m,g,gr))
    }

    private fun flushSilent(){
        if(silentQueue.isNotEmpty()){
            waMode = true
            isBusy = true

            queue.addAll(silentQueue)
            silentQueue.clear()

            selected = 0
            waitSelect = false
            waitReply = true

            AppSpeak.speak("Ada pesan masuk")
            speakChat(queue[0])
        }
    }

    // ===============================
    // PARSE NUMBER
    // ===============================

    private fun parseNumber(t:String):Int{
        return when {
            t.contains("1") || t.contains("satu") -> 0
            t.contains("2") || t.contains("dua") -> 1
            t.contains("3") || t.contains("tiga") -> 2
            else -> -1
        }
    }
}