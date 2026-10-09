package br.com.uri.meuprojeto.favorite

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val firestore = firestore

    fun addFavorite(
        userId: String,
        eventId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (!validateIds(userId, eventId, onError)) {
            return
        }

        val favoriteData = mapOf(
            "eventId" to eventId,
            "userId" to userId,
            "createdAt" to FieldValue.serverTimestamp()
        )

        favoritesCollection(userId)
            .document(eventId)
            .set(favoriteData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao adicionar favorito", exception)
                onError(exception)
            }
    }

    fun removeFavorite(
        userId: String,
        eventId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (!validateIds(userId, eventId, onError)) {
            return
        }

        favoritesCollection(userId)
            .document(eventId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao remover favorito", exception)
                onError(exception)
            }
    }

    fun isFavorite(
        userId: String,
        eventId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (!validateIds(userId, eventId, onError)) {
            return
        }

        favoritesCollection(userId)
            .document(eventId)
            .get()
            .addOnSuccessListener { document -> onSuccess(document.exists()) }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao consultar favorito", exception)
                onError(exception)
            }
    }

    fun getFavoriteEventIds(
        userId: String,
        onSuccess: (Set<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (!validateUserId(userId, onError)) {
            return
        }

        favoritesCollection(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.documents.map { document -> document.id }.toSet())
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao consultar favoritos", exception)
                onError(exception)
            }
    }

    private fun favoritesCollection(userId: String) =
        firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(FAVORITES_COLLECTION)

    private fun validateUserId(
        userId: String,
        onError: (Exception) -> Unit
    ): Boolean {
        if (userId.isNotBlank()) {
            return true
        }

        onError(IllegalArgumentException("userId não pode ser vazio"))
        return false
    }

    private fun validateIds(
        userId: String,
        eventId: String,
        onError: (Exception) -> Unit
    ): Boolean {
        if (validateUserId(userId, onError) && eventId.isNotBlank()) {
            return true
        }

        if (userId.isNotBlank()) {
            onError(IllegalArgumentException("eventId não pode ser vazio"))
        }
        return false
    }

    private companion object {
        const val TAG = "FavoriteRepository"
        const val USERS_COLLECTION = "users"
        const val FAVORITES_COLLECTION = "favorites"
    }
}
