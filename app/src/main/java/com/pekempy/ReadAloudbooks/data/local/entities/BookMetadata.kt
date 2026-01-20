package com.pekempy.ReadAloudbooks.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_metadata")
data class BookMetadata(
    @PrimaryKey
    val bookId: String,
    val readingStatus: ReadingStatus = ReadingStatus.NONE,
    val rating: Int? = null, // 1-5 stars
    val dateStarted: Long? = null,
    val dateFinished: Long? = null,
    val isFavorite: Boolean = false,
    val customNotes: String? = null,
    val syncedToServer: Boolean = false
)

enum class ReadingStatus {
    NONE,
    WANT_TO_READ,
    READING,
    FINISHED,
    DNF // Did Not Finish
}
