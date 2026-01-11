package com.pekempy.ReadAloudbooks.data

data class UnifiedProgress(
    val chapterIndex: Int,
    val elementId: String?,
    val audioTimestampMs: Long,
    val scrollPercent: Float,
    val lastUpdated: Long,
    val totalChapters: Int,
    val totalDurationMs: Long = 0L
) {
    fun getOverallProgress(): Float {
        if (totalDurationMs > 0) {
            return (audioTimestampMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
        }
        if (totalChapters <= 0) return 0f
        if (totalChapters == 1) return scrollPercent.coerceIn(0f, 1f)
        return (chapterIndex + scrollPercent.coerceIn(0f, 1f)) / totalChapters
    }

    companion object {
        fun fromString(progressStr: String?): UnifiedProgress? {
            if (progressStr.isNullOrBlank()) return null
            
            val parts = progressStr.split(":")
            if (parts.isEmpty()) return null
            
            return try {
                if (parts.size >= 7) {
                    UnifiedProgress(
                        chapterIndex = parts[0].toIntOrNull() ?: 0,
                        elementId = parts[1].takeIf { it.isNotBlank() },
                        audioTimestampMs = parts[2].toLongOrNull() ?: 0L,
                        scrollPercent = parts[3].toFloatOrNull() ?: 0f,
                        lastUpdated = parts[4].toLongOrNull() ?: System.currentTimeMillis(),
                        totalChapters = parts[5].toIntOrNull() ?: 1,
                        totalDurationMs = parts[6].toLongOrNull() ?: 0L
                    )
                }
                else if (parts.size >= 6) {
                    UnifiedProgress(
                        chapterIndex = parts[0].toIntOrNull() ?: 0,
                        elementId = parts[1].takeIf { it.isNotBlank() },
                        audioTimestampMs = parts[2].toLongOrNull() ?: 0L,
                        scrollPercent = parts[3].toFloatOrNull() ?: 0f,
                        lastUpdated = parts[4].toLongOrNull() ?: System.currentTimeMillis(),
                        totalChapters = parts[5].toIntOrNull() ?: 1
                    )
                }
                else if (parts.size >= 5) {
                    UnifiedProgress(
                        chapterIndex = parts[0].toIntOrNull() ?: 0,
                        elementId = null,
                        audioTimestampMs = parts[4].toLongOrNull() ?: 0L,
                        scrollPercent = parts[1].toFloatOrNull() ?: 0f,
                        lastUpdated = parts[2].toLongOrNull() ?: System.currentTimeMillis(),
                        totalChapters = parts[3].toIntOrNull() ?: 1
                    )
                }
                else if (parts.size >= 4) {
                    UnifiedProgress(
                        chapterIndex = parts[0].toIntOrNull() ?: 0,
                        elementId = null,
                        audioTimestampMs = 0L,
                        scrollPercent = parts[1].toFloatOrNull() ?: 0f,
                        lastUpdated = parts[2].toLongOrNull() ?: System.currentTimeMillis(),
                        totalChapters = parts[3].toIntOrNull() ?: 1
                    )
                }
                else if (parts.size >= 2) {
                    UnifiedProgress(
                        chapterIndex = parts[0].toIntOrNull() ?: 0,
                        elementId = null,
                        audioTimestampMs = 0L,
                        scrollPercent = parts[1].toFloatOrNull() ?: 0f,
                        lastUpdated = System.currentTimeMillis(),
                        totalChapters = 1
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("UnifiedProgress", "Failed to parse progress: $progressStr", e)
                null
            }
        }
    }
    
    override fun toString(): String {
        val elementIdPart = elementId ?: ""
        return "$chapterIndex:$elementIdPart:$audioTimestampMs:$scrollPercent:$lastUpdated:$totalChapters:$totalDurationMs"
    }
    
    fun withAudioPosition(
        newAudioTimestampMs: Long,
        newElementId: String?,
        newChapterIndex: Int? = null
    ): UnifiedProgress {
        return copy(
            chapterIndex = newChapterIndex ?: chapterIndex,
            elementId = newElementId,
            audioTimestampMs = newAudioTimestampMs,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    fun withScrollPosition(
        newChapterIndex: Int,
        newScrollPercent: Float,
        newElementId: String?,
        newAudioTimestampMs: Long?
    ): UnifiedProgress {
        return copy(
            chapterIndex = newChapterIndex,
            elementId = newElementId,
            audioTimestampMs = newAudioTimestampMs ?: audioTimestampMs,
            scrollPercent = newScrollPercent,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
