package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_documents")
data class SavedDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // e.g., "Identity", "Career", "Education", "Office", "Personal"
    val subCategory: String, // e.g., "NID Card", "CV", "Student ID", "Invoice"
    val detailsJson: String, // Stores serialized key-value pairs for fields
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val passwordLock: String? = null // Optional password lock protection
)
