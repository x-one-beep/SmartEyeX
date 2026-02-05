package com.smarteyex.core.voice

import com.smarteyex.core.AppState

object CurhatEngine {

    fun respond(emotion: AppState.Emotion): String {
        return when (emotion) {
            AppState.Emotion.SAD ->
                "gue dengerin ya… lo nggak sendirian. pelan aja, nggak usah kuat-kuat."

            AppState.Emotion.TIRED ->
                "capek itu manusiawi. lo udah sejauh ini, itu aja udah keren."

            AppState.Emotion.ANGRY ->
                "marah tuh wajar. yang penting lo nggak nyakitin diri lo sendiri."

            AppState.Emotion.EMPTY ->
                "kalau lagi kosong, gue di sini. diem bareng juga gapapa."

            else ->
                "gue denger kok. lanjut aja ngomongnya."
        }
    }
}