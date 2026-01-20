package com.pekempy.ReadAloudbooks.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "collection_books",
    primaryKeys = ["collectionId", "bookId"],
    indices = [Index(value = ["collectionId"]), Index(value = ["bookId"])],
    foreignKeys = [
        ForeignKey(
            entity = Collection::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CollectionBook(
    val collectionId: Long,
    val bookId: String,
    val addedAt: Long = System.currentTimeMillis()
)
