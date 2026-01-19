package com.smarteyex.core.wa

enum class CommandType { REPLY, READ, IGNORE }

data class VoiceCommand(
    val type: CommandType,
    val target: String,
    val message: String = ""
)

object VoiceCommandParser {

    fun parse(text: String): VoiceCommand {
        val lower = text.lowercase()

        return when {
            lower.contains("jawab ke") -> {
                val parts = lower.split("jawab ke")
                val target = parts[1].trim().split(" ")[0]
                val msg = parts[1].replace(target, "").trim()
                VoiceCommand(CommandType.REPLY, target, msg)
            }

            lower.contains("read") -> {
                VoiceCommand(CommandType.READ, extractTarget(lower))
            }

            lower.contains("diemin") -> {
                VoiceCommand(CommandType.IGNORE, extractTarget(lower))
            }

            else -> VoiceCommand(CommandType.IGNORE, "")
        }
    }

    private fun extractTarget(text: String): String {
        return text.split(" ").last()
    }
}
