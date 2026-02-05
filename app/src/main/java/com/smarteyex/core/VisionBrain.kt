object VisionBrain {

    private var context = VisionContext(
        currentState = VisionState.OBSERVE,
        focusedObject = null,
        dangerLevel = 0
    )

    fun updateFocus(objectName: String) {
        context = context.copy(
            focusedObject = objectName,
            currentState = VisionState.ASSIST
        )
    }

    fun warn(reason: String) {
        context = context.copy(
            currentState = VisionState.WARN,
            dangerLevel = 8
        )
    }

    fun getContext(): VisionContext = context
}