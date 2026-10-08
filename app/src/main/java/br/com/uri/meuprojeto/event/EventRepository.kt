package br.com.uri.meuprojeto.event

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class EventRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val eventsCollection = firestore.collection(EVENTS_COLLECTION)

    fun getEvents(
        onSuccess: (List<Event>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        eventsCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val events = snapshot.documents.mapNotNull { document ->
                    val title = document.getString("title")
                    val description = document.getString("description")
                    val date = document.getString("date")
                    val location = document.getString("location")
                    val startsAt = document.getOptionalTimestamp("startsAt")
                    val category = document.getOptionalString("category")

                    if (
                        title != null &&
                        description != null &&
                        date != null &&
                        location != null
                    ) {
                        Event(
                            id = document.id,
                            title = title,
                            description = description,
                            date = date,
                            location = location,
                            startsAt = startsAt,
                            category = category
                        )
                    } else {
                        Log.w(
                            TAG,
                            "Evento ${document.id} ignorado por possuir campos ausentes"
                        )
                        null
                    }
                }

                onSuccess(events)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao carregar eventos do Firestore", exception)
                onError(exception)
            }
    }

    private fun DocumentSnapshot.getOptionalTimestamp(field: String): Timestamp? =
        try {
            getTimestamp(field)
        } catch (exception: RuntimeException) {
            Log.w(
                TAG,
                "Campo $field do evento $id possui um tipo inválido; usando null",
                exception
            )
            null
        }

    private fun DocumentSnapshot.getOptionalString(field: String): String? =
        try {
            getString(field)
        } catch (exception: RuntimeException) {
            Log.w(
                TAG,
                "Campo $field do evento $id possui um tipo inválido; usando null",
                exception
            )
            null
        }

    private companion object {
        const val TAG = "EventRepository"
        const val EVENTS_COLLECTION = "events"
    }
}
