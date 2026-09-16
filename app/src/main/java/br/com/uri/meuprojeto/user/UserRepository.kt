package br.com.uri.meuprojeto.user

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun saveUserProfile(
        uid: String,
        name: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val profile = mapOf(
            "name" to name,
            "email" to email
        )

        firestore
            .collection("users")
            .document(uid)
            .set(profile)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao salvar perfil no Firestore", exception)
                onError(exception)
            }
    }

    private companion object {
        const val TAG = "UserRepository"
    }
}
