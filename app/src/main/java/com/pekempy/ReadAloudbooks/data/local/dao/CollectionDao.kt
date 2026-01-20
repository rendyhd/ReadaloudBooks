package com.pekempy.ReadAloudbooks.data.local.dao

import androidx.room.*
import com.pekempy.ReadAloudbooks.data.local.entities.BookCollection
import com.pekempy.ReadAloudbooks.data.local.entities.BookCollectionBook
import kotlinx.coroutines.flow.Flow

@Dao
interface BookCollectionDao {
    @Query("SELECT * FROM collections ORDER BY updatedAt DESC")
    fun getAllBookCollections(): Flow<List<BookCollection>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getBookCollectionById(id: Long): BookCollection?

    @Query("SELECT * FROM collections WHERE id = :id")
    fun getBookCollectionByIdFlow(id: Long): Flow<BookCollection?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookCollection(collection: BookCollection): Long

    @Update
    suspend fun updateBookCollection(collection: BookCollection)

    @Delete
    suspend fun deleteBookCollection(collection: BookCollection)

    // BookCollectionBook operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookCollectionBook(collectionBook: BookCollectionBook)

    @Delete
    suspend fun deleteBookCollectionBook(collectionBook: BookCollectionBook)

    @Query("SELECT bookId FROM collection_books WHERE collectionId = :collectionId ORDER BY addedAt DESC")
    fun getBooksInBookCollection(collectionId: Long): Flow<List<String>>

    @Query("SELECT collectionId FROM collection_books WHERE bookId = :bookId")
    fun getBookCollectionsForBook(bookId: String): Flow<List<Long>>

    @Query("DELETE FROM collection_books WHERE collectionId = :collectionId")
    suspend fun removeAllBooksFromBookCollection(collectionId: Long)

    @Query("DELETE FROM collection_books WHERE bookId = :bookId")
    suspend fun removeBookFromAllBookCollections(bookId: String)

    @Query("SELECT COUNT(*) FROM collection_books WHERE collectionId = :collectionId")
    suspend fun getBookCountInBookCollection(collectionId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM collection_books WHERE collectionId = :collectionId AND bookId = :bookId)")
    suspend fun isBookInBookCollection(collectionId: Long, bookId: String): Boolean

    @Query("SELECT * FROM collections WHERE syncedToServer = 0")
    suspend fun getUnsyncedBookCollections(): List<BookCollection>

    @Query("UPDATE collections SET syncedToServer = 1, serverId = :serverId WHERE id = :id")
    suspend fun markAsSynced(id: Long, serverId: String)
}
