package br.com.uri.meuprojeto.event

import android.util.Log
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
                            location = location
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

    private companion object {
        const val TAG = "EventRepository"
        const val EVENTS_COLLECTION = "events"
    }
}
