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
        val identifiers = normalizeIdentifiers(userId, eventId, onError) ?: return
        val normalizedUserId = identifiers.first
        val normalizedEventId = identifiers.second
        val registrationId = registrationDocumentId(
            normalizedUserId,
            normalizedEventId
        )
        val registeredAt = System.currentTimeMillis()
        val registrationData = mapOf(
            "userId" to normalizedUserId,
            "eventId" to normalizedEventId,
            "registeredAt" to registeredAt
        )

        registrationsCollection
            .document(registrationId)
            .set(registrationData)
            .addOnSuccessListener {
                onSuccess(
                    Registration(
                        id = registrationId,
                        userId = normalizedUserId,
                        eventId = normalizedEventId,
                        registeredAt = registeredAt
                    )
                )
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao criar inscrição", exception)
                onError(exception)
            }
    }

    fun getRegistration(
        userId: String,
        eventId: String,
        onSuccess: (Registration?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val identifiers = normalizeIdentifiers(userId, eventId, onError) ?: return
        val normalizedUserId = identifiers.first
        val normalizedEventId = identifiers.second
        val registrationId = registrationDocumentId(
            normalizedUserId,
            normalizedEventId
        )

        registrationsCollection
            .document(registrationId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
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
                } else {
                    getLegacyRegistration(
                        userId = normalizedUserId,
                        eventId = normalizedEventId,
                        onSuccess = onSuccess,
                        onError = onError
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao consultar inscrição", exception)
                onError(exception)
            }
    }

    fun getRegistrations(
        userId: String,
        onSuccess: (List<Registration>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val normalizedUserId = normalizeIdentifier(userId, "userId", onError)
            ?: return

        registrationsCollection
            .whereEqualTo("userId", normalizedUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                val registrations = snapshot.documents.mapNotNull { document ->
                    val registration = document.toRegistration()
                    if (registration == null) {
                        Log.w(
                            TAG,
                            "Inscrição ${document.id} ignorada por possuir campos inválidos"
                        )
                    }
                    registration
                }

                onSuccess(registrations)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao consultar inscrições do usuário", exception)
                onError(exception)
            }
    }

    fun deleteRegistration(
        registrationId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val normalizedRegistrationId = normalizeIdentifier(
            registrationId,
            "registrationId",
            onError
        ) ?: return

        registrationsCollection
            .document(normalizedRegistrationId)
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

    private fun getLegacyRegistration(
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
                Log.e(TAG, "Erro ao consultar inscrições antigas", exception)
                onError(exception)
            }
    }

    private fun normalizeIdentifiers(
        userId: String,
        eventId: String,
        onError: (Exception) -> Unit
    ): Pair<String, String>? {
        val normalizedUserId = normalizeIdentifier(userId, "userId", onError)
            ?: return null
        val normalizedEventId = normalizeIdentifier(eventId, "eventId", onError)
            ?: return null

        try {
            registrationDocumentId(normalizedUserId, normalizedEventId)
        } catch (exception: IllegalArgumentException) {
            onError(exception)
            return null
        }

        return normalizedUserId to normalizedEventId
    }

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
            onError(
                IllegalArgumentException(
                    "$fieldName não pode conter '/'")
            )
            return null
        }
        return normalizedValue
    }

    private companion object {
        const val TAG = "RegistrationRepository"
        const val REGISTRATIONS_COLLECTION = "registrations"
    }
}

internal fun registrationDocumentId(
    userId: String,
    eventId: String
): String {
    require(userId.isNotBlank()) { "userId não pode ser vazio" }
    require(eventId.isNotBlank()) { "eventId não pode ser vazio" }
    require('/' !in userId) { "userId não pode conter '/'" }
    require('/' !in eventId) { "eventId não pode conter '/'" }

    return "${userId.trim()}_${eventId.trim()}"
}
