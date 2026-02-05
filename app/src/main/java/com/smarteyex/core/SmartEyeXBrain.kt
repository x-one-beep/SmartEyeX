object SmartEyeXBrain {

    fun onWaMessageReceived(sender: String, message: String) {
        if (AppState.isBusyMode) return

        if (PriorityResolver.isHighPriority(sender, message)) {
            VoiceEngine.speak(
                "Ada pesan dari $sender. ${message.take(80)}"
            )
        }
    }

    fun onUserVoiceReply(text: String) {
        WaReplyAccessibilityService.sendReply(text)
    }
}