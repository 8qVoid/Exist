package com.exist.app.domain.model

enum class EmotionTag(val displayName: String) {
    CALM("Calm"),
    HAPPY("Happy"),
    SAD("Sad"),
    ANGRY("Angry"),
    TIRED("Tired"),
    OVERWHELMED("Overwhelmed"),
    GRATEFUL("Grateful"),
    NEUTRAL("Neutral");

    companion object {
        fun fromName(name: String): EmotionTag =
            entries.firstOrNull { it.name == name } ?: NEUTRAL
    }
}
