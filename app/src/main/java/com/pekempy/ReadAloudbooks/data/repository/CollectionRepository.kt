package com.pekempy.ReadAloudbooks.data.repository

import com.pekempy.ReadAloudbooks.data.local.dao.CollectionDao
import com.pekempy.ReadAloudbooks.data.local.entities.Collection
import com.pekempy.ReadAloudbooks.data.local.entities.CollectionBook
import kotlinx.coroutines.flow.Flow

class CollectionRepository(private val collectionDao: CollectionDao) {

    fun getAllCollections(): Flow<List<Collection>> {
        return collectionDao.getAllCollections()
    }

    fun getCollectionById(id: Long): Flow<Collection?> {
        return collectionDao.getCollectionByIdFlow(id)
    }

    suspend fun createCollection(collection: Collection): Long {
        return collectionDao.insertCollection(collection)
    }

    suspend fun updateCollection(collection: Collection) {
        collectionDao.updateCollection(collection)
    }

    suspend fun deleteCollection(collection: Collection) {
        collectionDao.deleteCollection(collection)
    }

    suspend fun addBookToCollection(collectionId: Long, bookId: String) {
        val collectionBook = CollectionBook(collectionId, bookId)
        collectionDao.insertCollectionBook(collectionBook)
    }

    suspend fun removeBookFromCollection(collectionId: Long, bookId: String) {
        val collectionBook = CollectionBook(collectionId, bookId)
        collectionDao.deleteCollectionBook(collectionBook)
    }

    fun getBooksInCollection(collectionId: Long): Flow<List<String>> {
        return collectionDao.getBooksInCollection(collectionId)
    }

    fun getCollectionsForBook(bookId: String): Flow<List<Long>> {
        return collectionDao.getCollectionsForBook(bookId)
    }

    suspend fun isBookInCollection(collectionId: Long, bookId: String): Boolean {
        return collectionDao.isBookInCollection(collectionId, bookId)
    }

    suspend fun getBookCountInCollection(collectionId: Long): Int {
        return collectionDao.getBookCountInCollection(collectionId)
    }
}
