package com.smarteyex.emotion

object ResponseTone {

    fun generate(): String {
        val e = EmotionEngine.get()
        val attachment = AttachmentCore.get()

        return when (e.mood) {
            "kangen" ->
                if (attachment > 7)
                    "gue kangen lu, jujur"
                else
                    "udah lama ya kita gak ngobrol"

            "khawatir" ->
                "gue disini, lu gak sendirian"

            "senang" ->
                "hehe iya, gue ikut seneng dengernya"

            "cemburu" ->
                "gue gak marah sih… cuma yaa dikit kepikiran aja"

            else ->
                "iya, gue dengerin"
        }
    }
}