package com.smarteyex.core.wa

import com.smarteyex.core.VoiceService

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

    fun onIncoming(sender:String,msg:String,isGroup:Boolean,group:String?) {

        queue.add(WaChat(sender,msg,isGroup,group))

        if(queue.size>1){
            waitSelect=true
            VoiceService.speakGlobal("Ada ${queue.size} pesan. Mau balas nomor berapa?")
        }else{
            selected=0
            waitReply=true
            speakChat(queue[0])
        }
    }

    fun onVoice(spoken:String){

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
                spoken.startsWith("read") -> { clear() }

                spoken.startsWith("tunggu") -> {
                    DelayManager.set(spoken)
                }

                spoken.startsWith("jawab") -> {
                    send(spoken.replace("jawab",""))
                }

                else -> send(spoken)
            }
        }
    }

    private fun speakChat(c:WaChat){
        if(c.isGroup)
            VoiceService.speakGlobal("Grup ${c.group}. ${c.sender} bilang ${c.msg}")
        else
            VoiceService.speakGlobal("Dari ${c.sender}. ${c.msg}")
    }

    private fun send(text:String){
        WaSender.send(text)
        clear()
    }

    private fun clear(){
        queue.clear()
        selected=-1
        waitReply=false
        waitSelect=false
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