package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM saved_documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<SavedDocument>>

    @Query("SELECT * FROM saved_documents WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteDocuments(): Flow<List<SavedDocument>>

    @Query("SELECT * FROM saved_documents WHERE id = :id")
    suspend fun getDocumentById(id: Int): SavedDocument?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: SavedDocument): Long

    @Update
    suspend fun updateDocument(document: SavedDocument)

    @Delete
    suspend fun deleteDocument(document: SavedDocument)

    @Query("DELETE FROM saved_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Int)
}
