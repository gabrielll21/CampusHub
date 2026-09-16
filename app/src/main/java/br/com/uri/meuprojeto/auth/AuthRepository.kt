package br.com.uri.meuprojeto.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun registerUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (RegistrationError) -> Unit
    ) {
        firebaseAuth
            .createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    onSuccess(uid)
                } else {
                    onError(RegistrationError.UNKNOWN)
                }
            }
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

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (LoginError) -> Unit
    ) {
        firebaseAuth
            .signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao autenticar usuário", exception)
                val error = when (exception) {
                    is FirebaseAuthInvalidCredentialsException,
                    is FirebaseAuthInvalidUserException -> LoginError.INVALID_CREDENTIALS
                    else -> LoginError.UNKNOWN
                }
                onError(error)
            }
    }

    private companion object {
        const val TAG = "AuthRepository"
    }
}

enum class RegistrationError {
    INVALID_EMAIL,
    WEAK_PASSWORD,
    EMAIL_ALREADY_IN_USE,
    UNKNOWN
}

enum class LoginError {
    INVALID_CREDENTIALS,
    UNKNOWN
}
