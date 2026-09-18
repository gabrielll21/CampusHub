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

    fun getUserProfile(
        uid: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("name")
                val email = document.getString("email")

                if (document.exists() && name != null && email != null) {
                    onSuccess(UserProfile(name = name, email = email))
                } else {
                    val exception =
                        IllegalStateException("Perfil do usuário incompleto ou ausente")
                    Log.e(TAG, "Erro ao carregar perfil do Firestore", exception)
                    onError(exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao carregar perfil do Firestore", exception)
                onError(exception)
            }
    }

    fun updateUserName(
        uid: String,
        name: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore
            .collection("users")
            .document(uid)
            .update("name", name)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao atualizar nome no Firestore", exception)
                onError(exception)
            }
    }

    private companion object {
        const val TAG = "UserRepository"
    }
}

data class UserProfile(
    val name: String,
    val email: String
)
