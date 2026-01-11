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
    val uuid: String
)

data class Position(
    val uuid: String? = null,
    val percentage: Double,
    val completed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String 
)
