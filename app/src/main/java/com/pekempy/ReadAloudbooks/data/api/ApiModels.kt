package com.pekempy.ReadAloudbooks.data.api

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class BookResponse(
    val uuid: String,
    val title: String,
    val description: String?,
    val authors: List<AuthorResponse>,
    val narrators: List<NarratorResponse>?,
    val series: List<SeriesResponse>?,
    val collections: List<SeriesResponse>?,
    val audiobook: AudiobookResponse?,
    val ebook: EbookResponse?,
    @SerializedName("readaloud") val ReadAloud: ReadAloudResponse?
)

data class SeriesResponse(
    val uuid: String,
    val name: String,
    @SerializedName("index") val index: Any? = null,
    @SerializedName("sequence") val sequence: Any? = null,
    @SerializedName("position") val position: Any? = null,
    @SerializedName("series_index") val series_index: Any? = null,
    @SerializedName("seriesIndex") val seriesIndexField: Any? = null
) {
    val seriesIndex: String?
        get() {
            val raw = index ?: sequence ?: position ?: series_index ?: seriesIndexField
            if (raw == null) return null
            
            val s = raw.toString()
            val cleanS = if (s.endsWith(".0")) s.substringBefore(".0") else s
            
            return if (cleanS.isBlank() || cleanS.equals("null", ignoreCase = true)) null else cleanS
        }
}

data class AuthorResponse(
    val uuid: String,
    val name: String
)

data class NarratorResponse(
    val uuid: String,
    val name: String
)

data class AudiobookResponse(
    val uuid: String,
    val filepath: String?
)

data class EbookResponse(
    val uuid: String,
    val filepath: String?
)

data class ReadAloudResponse(
    val uuid: String,
    val status: String? = null,
    val filepath: String? = null,
    val queuePosition: Int? = null,
    val currentStage: String? = null,
    val stageProgress: Double? = null
)

data class Position(
    val locator: Locator,
    val timestamp: Long = System.currentTimeMillis()
)

data class Locator(
    val href: String,
    @SerializedName("type") val mediaType: String,
    val title: String? = null,
    val locations: Locations
)

data class Locations(
    val progression: Double,
    val position: Int? = null,
    @SerializedName("totalProgression") val totalProgression: Double? = null,
    
    // Additional fields for local storage and unified progress mapping
    val audioTimestampMs: Long? = null,
    val chapterIndex: Int? = null,
    val elementId: String? = null,
    val totalChapters: Int? = null,
    val totalDurationMs: Long? = null
)
