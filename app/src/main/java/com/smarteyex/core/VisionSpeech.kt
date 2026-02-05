object VisionSpeech {

    fun speak(context: VisionContext): String {
        return when (context.currentState) {
            VisionState.WARN ->
                "bentar, itu bahaya"

            VisionState.ASSIST ->
                "itu bagian ${context.focusedObject}"

            VisionState.GUIDE ->
                "lanjut, tapi pelan ya"

            else -> ""
        }
    }
}