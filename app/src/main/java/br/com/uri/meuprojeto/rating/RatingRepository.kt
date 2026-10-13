package br.com.uri.meuprojeto.rating

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RatingRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val ratingsCollection = firestore.collection(EVENTS_COLLECTION)

    fun setRating(
        eventId: String,
        userId: String,
        score: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val normalizedEventId = normalizeIdentifier(eventId, "eventId", onError)
            ?: return
        val normalizedUserId = normalizeIdentifier(userId, "userId", onError)
            ?: return

        try {
            validateRatingScore(score)
        } catch (exception: IllegalArgumentException) {
            onError(exception)
            return
        }

        val ratingReference = ratingDocument(normalizedEventId, normalizedUserId)
        ratingReference
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    ratingReference
                        .update(
                            mapOf(
                                "score" to score,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Erro ao atualizar avaliação", exception)
                            onError(exception)
                        }
                } else {
                    ratingReference
                        .set(
                            mapOf(
                                "userId" to normalizedUserId,
                                "eventId" to normalizedEventId,
                                "score" to score,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to null
                            )
                        )
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Erro ao criar avaliação", exception)
                            onError(exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao consultar avaliação antes da gravação", exception)
                onError(exception)
            }
    }

    fun getUserRating(
        eventId: String,
        userId: String,
        onSuccess: (Rating?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val normalizedEventId = normalizeIdentifier(eventId, "eventId", onError)
            ?: return
        val normalizedUserId = normalizeIdentifier(userId, "userId", onError)
            ?: return

        ratingDocument(normalizedEventId, normalizedUserId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onSuccess(null)
                    return@addOnSuccessListener
                }

                val rating = document.toRating(
                    eventId = normalizedEventId,
                    userId = normalizedUserId
                )
                if (rating != null) {
                    onSuccess(rating)
                } else {
                    val exception = IllegalStateException(
                        "Documento de avaliação incompleto ou inválido: ${document.id}"
                    )
                    Log.e(TAG, "Erro ao mapear avaliação", exception)
                    onError(exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao consultar avaliação do usuário", exception)
                onError(exception)
            }
    }

    fun getRatings(
        eventId: String,
        onSuccess: (List<Rating>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val normalizedEventId = normalizeIdentifier(eventId, "eventId", onError)
            ?: return

        ratingsCollection
            .document(normalizedEventId)
            .collection(RATINGS_COLLECTION)
            .get()
            .addOnSuccessListener { snapshot ->
                val ratings = snapshot.documents.mapNotNull { document ->
                    val rating = document.toRating(eventId = normalizedEventId)
                    if (rating == null) {
                        Log.w(
                            TAG,
                            "Avaliação ${document.id} ignorada por possuir campos inválidos"
                        )
                    }
                    rating
                }
                onSuccess(ratings)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao consultar avaliações do evento", exception)
                onError(exception)
            }
    }

    private fun ratingDocument(eventId: String, userId: String) =
        ratingsCollection
            .document(eventId)
            .collection(RATINGS_COLLECTION)
            .document(userId)

    private fun normalizeIdentifier(
        value: String,
        fieldName: String,
        onError: (Exception) -> Unit
    ): String? {
        val normalizedValue = value.trim()
        if (normalizedValue.isEmpty()) {
            onError(IllegalArgumentException("$fieldName não pode ser vazio"))
            return null
        }
        if ('/' in normalizedValue) {
            onError(IllegalArgumentException("$fieldName não pode conter '/'"))
            return null
        }
        return normalizedValue
    }

    private fun DocumentSnapshot.toRating(
        eventId: String,
        userId: String? = null
    ): Rating? {
        val storedEventId = getString("eventId")
        val storedUserId = getString("userId")
        val score = getLong("score")?.toInt()

        if (
            storedEventId.isNullOrBlank() ||
            storedEventId != eventId ||
            storedUserId.isNullOrBlank() ||
            storedUserId != id ||
            (userId != null && storedUserId != userId) ||
            score == null ||
            score !in RATING_SCORE_RANGE
        ) {
            return null
        }

        return Rating(
            userId = storedUserId,
            eventId = storedEventId,
            score = score,
            createdAt = getOptionalTimestamp("createdAt"),
            updatedAt = getOptionalTimestamp("updatedAt")
        )
    }

    private fun DocumentSnapshot.getOptionalTimestamp(field: String): Timestamp? =
        try {
            getTimestamp(field)
        } catch (exception: RuntimeException) {
            Log.w(
                TAG,
                "Campo $field da avaliação $id possui um tipo inválido; usando null",
                exception
            )
            null
        }

    private companion object {
        const val TAG = "RatingRepository"
        const val EVENTS_COLLECTION = "events"
        const val RATINGS_COLLECTION = "ratings"
        val RATING_SCORE_RANGE = 1..5
    }
}

internal fun validateRatingScore(score: Int) {
    require(score in 1..5) { "score deve estar entre 1 e 5" }
}
