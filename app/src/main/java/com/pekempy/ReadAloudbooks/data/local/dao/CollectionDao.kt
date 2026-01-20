package com.pekempy.ReadAloudbooks.data.local.dao

import androidx.room.*
import com.pekempy.ReadAloudbooks.data.local.entities.Collection
import com.pekempy.ReadAloudbooks.data.local.entities.CollectionBook
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY updatedAt DESC")
    fun getAllCollections(): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollectionById(id: Long): Collection?

    @Query("SELECT * FROM collections WHERE id = :id")
    fun getCollectionByIdFlow(id: Long): Flow<Collection?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: Collection): Long

    @Update
    suspend fun updateCollection(collection: Collection)

    @Delete
    suspend fun deleteCollection(collection: Collection)

    // CollectionBook operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionBook(collectionBook: CollectionBook)

    @Delete
    suspend fun deleteCollectionBook(collectionBook: CollectionBook)

    @Query("SELECT bookId FROM collection_books WHERE collectionId = :collectionId ORDER BY addedAt DESC")
    fun getBooksInCollection(collectionId: Long): Flow<List<String>>

    @Query("SELECT collectionId FROM collection_books WHERE bookId = :bookId")
    fun getCollectionsForBook(bookId: String): Flow<List<Long>>

    @Query("DELETE FROM collection_books WHERE collectionId = :collectionId")
    suspend fun removeAllBooksFromCollection(collectionId: Long)

    @Query("DELETE FROM collection_books WHERE bookId = :bookId")
    suspend fun removeBookFromAllCollections(bookId: String)

    @Query("SELECT COUNT(*) FROM collection_books WHERE collectionId = :collectionId")
    suspend fun getBookCountInCollection(collectionId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM collection_books WHERE collectionId = :collectionId AND bookId = :bookId)")
    suspend fun isBookInCollection(collectionId: Long, bookId: String): Boolean

    @Query("SELECT * FROM collections WHERE syncedToServer = 0")
    suspend fun getUnsyncedCollections(): List<Collection>

    @Query("UPDATE collections SET syncedToServer = 1, serverId = :serverId WHERE id = :id")
    suspend fun markAsSynced(id: Long, serverId: String)
}
