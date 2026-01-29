package com.smarteyex.core.wa

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

object WaBus {
    var onMessage:(String,String,Boolean,String?)->Unit = {_,_,_,_->}
}

class WaNotificationListener: NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if(sbn.packageName != "com.whatsapp") return

        val extras = sbn.notification.extras
        val msg = extras.getCharSequence("android.text")?.toString()
    ?: extras.getCharSequence("android.bigText")?.toString()
    ?: return

val title = extras.getCharSequence("android.title")?.toString() ?: return

        val isGroup = title.contains(":")
        val sender = if(isGroup) title.substringAfter(":").trim() else title
        val group = if(isGroup) title.substringBefore(":") else null
if(AppState.speaking){
    WaReplyManager.queueSilent(sender,msg,isGroup,group)
}else{
    WaReplyManager.onIncoming(sender,msg,isGroup,group)
}
        WaBus.onMessage(sender,msg,isGroup,group)
    }
}