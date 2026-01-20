package com.pekempy.ReadAloudbooks.data.repository

import com.pekempy.ReadAloudbooks.data.local.dao.BookMetadataDao
import com.pekempy.ReadAloudbooks.data.local.entities.BookMetadata
import com.pekempy.ReadAloudbooks.data.local.entities.ReadingStatus
import kotlinx.coroutines.flow.Flow

class BookMetadataRepository(private val bookMetadataDao: BookMetadataDao) {

    fun getBookMetadata(bookId: String): Flow<BookMetadata?> {
        return bookMetadataDao.getBookMetadata(bookId)
    }

    suspend fun getBookMetadataSync(bookId: String): BookMetadata? {
        return bookMetadataDao.getBookMetadataSync(bookId)
    }

    suspend fun updateBookMetadata(bookMetadata: BookMetadata) {
        bookMetadataDao.insertBookMetadata(bookMetadata)
    }

    suspend fun updateReadingStatus(bookId: String, status: ReadingStatus) {
        val metadata = bookMetadataDao.getBookMetadataSync(bookId) ?: BookMetadata(bookId = bookId)
        val updatedMetadata = metadata.copy(
            readingStatus = status,
            dateStarted = if (status == ReadingStatus.READING && metadata.dateStarted == null) {
                System.currentTimeMillis()
            } else {
                metadata.dateStarted
            },
            dateFinished = if (status == ReadingStatus.FINISHED) {
                System.currentTimeMillis()
            } else {
                null
            },
            syncedToServer = false
        )
        bookMetadataDao.insertBookMetadata(updatedMetadata)
    }

    suspend fun setBookRating(bookId: String, rating: Int) {
        val metadata = bookMetadataDao.getBookMetadataSync(bookId) ?: BookMetadata(bookId = bookId)
        bookMetadataDao.insertBookMetadata(metadata.copy(rating = rating, syncedToServer = false))
    }

    suspend fun toggleFavorite(bookId: String) {
        val metadata = bookMetadataDao.getBookMetadataSync(bookId) ?: BookMetadata(bookId = bookId)
        bookMetadataDao.insertBookMetadata(
            metadata.copy(isFavorite = !metadata.isFavorite, syncedToServer = false)
        )
    }

    fun getBooksByStatus(status: ReadingStatus): Flow<List<BookMetadata>> {
        return bookMetadataDao.getBooksByStatus(status)
    }

    fun getFavoriteBooks(): Flow<List<BookMetadata>> {
        return bookMetadataDao.getFavoriteBooks()
    }

    fun getAllBookMetadata(): Flow<List<BookMetadata>> {
        return bookMetadataDao.getAllBookMetadata()
    }
}
