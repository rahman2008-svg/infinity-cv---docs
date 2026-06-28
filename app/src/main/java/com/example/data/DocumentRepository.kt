package com.example.data

import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val documentDao: DocumentDao) {
    val allDocuments: Flow<List<SavedDocument>> = documentDao.getAllDocuments()
    val favoriteDocuments: Flow<List<SavedDocument>> = documentDao.getFavoriteDocuments()

    suspend fun getDocumentById(id: Int): SavedDocument? {
        return documentDao.getDocumentById(id)
    }

    suspend fun insertDocument(document: SavedDocument): Long {
        return documentDao.insertDocument(document)
    }

    suspend fun updateDocument(document: SavedDocument) {
        documentDao.updateDocument(document)
    }

    suspend fun deleteDocument(document: SavedDocument) {
        documentDao.deleteDocument(document)
    }

    suspend fun deleteDocumentById(id: Int) {
        documentDao.deleteDocumentById(id)
    }
}
