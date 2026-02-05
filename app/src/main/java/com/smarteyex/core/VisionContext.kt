data class VisionContext(
    val currentState: VisionState,
    val focusedObject: String?,
    val dangerLevel: Int
)