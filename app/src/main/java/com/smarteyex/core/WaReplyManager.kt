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
    private var selected = -1
    private var waitSelect=false
    private var waitReply=false
var isBusy = false
private val voiceQueue = mutableListOf<String>()

    fun queueVoice(text: String) {
        voiceQueue.add(text)
if(spoken.isBlank()) return
if(waMode){
    onVoice(text)
}
    }

    fun onIncoming(sender:String,msg:String,isGroup:Boolean,group:String?) {
isBusy = true
waMode = true
        queue.add(WaChat(sender,msg,isGroup,group))

        if(queue.size>1){
            waitSelect=true
            AppSpeak.speak("Ada ${queue.size} pesan. Mau balas nomor berapa?")
        }else{
            selected=0
            waitReply=true
            speakChat(queue[0])
        }
    }

    fun onVoice(spoken:String){
if(spoken.isBlank()) return
  if(!waMode) return
        if(waitSelect){
            val i = parseNumber(spoken)
            if(i>=0){
                selected=i
                waitSelect=false
                waitReply=true
                speakChat(queue[i])
            }
            return
        }

        if(waitReply){

            when{
                spoken.startsWith("read") -> {
 isBusy = false
clear() }

                spoken.startsWith("tunggu") -> {
                    DelayManager.set(spoken)
isBusy = false
                }

                spoken.startsWith("jawab") -> {
                    send(spoken.replace("jawab",""))
                }

                else -> send(spoken)
            }
        }
    }

    private fun speakChat(c:WaChat){
isBusy = true
        if(c.isGroup)
    AppSpeak.speak("Grup ${c.group ?: "tidak dikenal"}. ${c.sender} bilang ${c.msg}")
        else
         AppSpeak.speak("Dari ${c.sender}. ${c.msg}")
    }

    private fun send(text:String){
    if(selected >= 0){
        WaSender.send(text)
    }
    isBusy = false
clear()
}

    private fun clear(){
    queue.clear()
voiceQueue.clear()
    selected = -1
    waitReply = false
    waitSelect = false
    waMode = false   // ⬅ MATIKAN MODE WA
isBusy = false
}

private var silentQueue = mutableListOf<WaChat>()

fun queueSilent(s:String,m:String,g:Boolean,gr:String?){
    silentQueue.add(WaChat(s,m,g,gr))
}

fun flushSilent(){
    if(silentQueue.isNotEmpty()){
waMode = true
isBusy = true
        queue.addAll(silentQueue)
        silentQueue.clear()
        selected = 0
        AppSpeak.speak("Wait, ada WhatsApp masuk")
        speakChat(queue[0])
        waitReply = true
    }
}

    private fun parseNumber(t:String):Int{
        return when{
            t.contains("1")||t.contains("satu")->0
            t.contains("2")||t.contains("dua")->1
            t.contains("3")||t.contains("tiga")->2
            else->-1
        }
    }
}