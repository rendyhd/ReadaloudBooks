package com.pekempy.ReadAloudbooks.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class BookCollection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val colorHex: String? = null, // Optional collection color
    val isSmartCollection: Boolean = false, // For rule-based collections
    val smartRules: String? = null, // JSON string of rules
    val syncedToServer: Boolean = false,
    val serverId: String? = null
)
