package br.com.uri.meuprojeto.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun registerUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (RegistrationError) -> Unit
    ) {
        firebaseAuth
            .createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                val error = when (exception) {
                    is FirebaseAuthWeakPasswordException -> RegistrationError.WEAK_PASSWORD
                    is FirebaseAuthInvalidCredentialsException -> RegistrationError.INVALID_EMAIL
                    is FirebaseAuthUserCollisionException -> RegistrationError.EMAIL_ALREADY_IN_USE
                    else -> RegistrationError.UNKNOWN
                }
                onError(error)
            }
    }
}

enum class RegistrationError {
    INVALID_EMAIL,
    WEAK_PASSWORD,
    EMAIL_ALREADY_IN_USE,
    UNKNOWN
}
