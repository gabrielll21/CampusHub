package br.com.uri.meuprojeto.registration

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val registrationsCollection =
        firestore.collection(REGISTRATIONS_COLLECTION)

    fun createRegistration(
        userId: String,
        eventId: String,
        onSuccess: (Registration) -> Unit,
        onError: (Exception) -> Unit
    ) {
        getRegistration(
            userId = userId,
            eventId = eventId,
            onSuccess = { existingRegistration ->
                if (existingRegistration != null) {
                    onSuccess(existingRegistration)
                    return@getRegistration
                }

                val registeredAt = System.currentTimeMillis()
                val registrationData = mapOf(
                    "userId" to userId,
                    "eventId" to eventId,
                    "registeredAt" to registeredAt
                )

                registrationsCollection
                    .add(registrationData)
                    .addOnSuccessListener { document ->
                        onSuccess(
                            Registration(
                                id = document.id,
                                userId = userId,
                                eventId = eventId,
                                registeredAt = registeredAt
                            )
                        )
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Erro ao criar inscrição", exception)
                        onError(exception)
                    }
            },
            onError = onError
        )
    }

    fun getRegistration(
        userId: String,
        eventId: String,
        onSuccess: (Registration?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        registrationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("eventId", eventId)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val document = snapshot.documents.firstOrNull()
                if (document == null) {
                    onSuccess(null)
                    return@addOnSuccessListener
                }

                val registration = document.toRegistration()
                if (registration != null) {
                    onSuccess(registration)
                } else {
                    val exception = IllegalStateException(
                        "Documento de inscrição incompleto ou inválido: ${document.id}"
                    )
                    Log.e(TAG, "Erro ao mapear inscrição", exception)
                    onError(exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao consultar inscrição", exception)
                onError(exception)
            }
    }

    fun deleteRegistration(
        registrationId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        registrationsCollection
            .document(registrationId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao excluir inscrição", exception)
                onError(exception)
            }
    }

    private fun DocumentSnapshot.toRegistration(): Registration? {
        val userId = getString("userId")
        val eventId = getString("eventId")
        val registeredAt = getLong("registeredAt")

        return if (userId != null && eventId != null && registeredAt != null) {
            Registration(
                id = id,
                userId = userId,
                eventId = eventId,
                registeredAt = registeredAt
            )
        } else {
            null
        }
    }

    private companion object {
        const val TAG = "RegistrationRepository"
        const val REGISTRATIONS_COLLECTION = "registrations"
    }
}
