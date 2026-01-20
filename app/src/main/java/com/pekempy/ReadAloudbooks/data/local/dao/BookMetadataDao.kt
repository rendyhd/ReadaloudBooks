package com.pekempy.ReadAloudbooks.data.local.dao

import androidx.room.*
import com.pekempy.ReadAloudbooks.data.local.entities.BookMetadata
import com.pekempy.ReadAloudbooks.data.local.entities.ReadingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookMetadataDao {
    @Query("SELECT * FROM book_metadata WHERE bookId = :bookId")
    fun getBookMetadata(bookId: String): Flow<BookMetadata?>

    @Query("SELECT * FROM book_metadata WHERE bookId = :bookId")
    suspend fun getBookMetadataSync(bookId: String): BookMetadata?

    @Query("SELECT * FROM book_metadata WHERE readingStatus = :status ORDER BY dateStarted DESC")
    fun getBooksByStatus(status: ReadingStatus): Flow<List<BookMetadata>>

    @Query("SELECT * FROM book_metadata WHERE isFavorite = 1 ORDER BY dateStarted DESC")
    fun getFavoriteBooks(): Flow<List<BookMetadata>>

    @Query("SELECT * FROM book_metadata WHERE rating IS NOT NULL ORDER BY rating DESC, dateFinished DESC")
    fun getRatedBooks(): Flow<List<BookMetadata>>

    @Query("SELECT COUNT(*) FROM book_metadata WHERE readingStatus = :status")
    suspend fun getCountByStatus(status: ReadingStatus): Int

    @Query("SELECT COUNT(*) FROM book_metadata WHERE readingStatus = 'FINISHED' AND dateFinished >= :startTime AND dateFinished <= :endTime")
    suspend fun getBooksFinishedInRange(startTime: Long, endTime: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookMetadata(bookMetadata: BookMetadata)

    @Update
    suspend fun updateBookMetadata(bookMetadata: BookMetadata)

    @Delete
    suspend fun deleteBookMetadata(bookMetadata: BookMetadata)

    @Query("DELETE FROM book_metadata WHERE bookId = :bookId")
    suspend fun deleteBookMetadataById(bookId: String)

    @Query("SELECT * FROM book_metadata WHERE syncedToServer = 0")
    suspend fun getUnsyncedMetadata(): List<BookMetadata>

    @Query("UPDATE book_metadata SET syncedToServer = 1 WHERE bookId = :bookId")
    suspend fun markAsSynced(bookId: String)

    @Query("SELECT * FROM book_metadata")
    fun getAllBookMetadata(): Flow<List<BookMetadata>>
}
