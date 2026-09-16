package br.com.uri.meuprojeto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import br.com.uri.meuprojeto.auth.AuthRepository
import br.com.uri.meuprojeto.auth.RegistrationError
import br.com.uri.meuprojeto.ui.RegistrationScreen
import br.com.uri.meuprojeto.ui.theme.MeuProjetoTheme
import br.com.uri.meuprojeto.user.UserRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeuProjetoTheme {
                val authRepository = remember { AuthRepository() }
                val userRepository = remember { UserRepository() }
                var isLoading by rememberSaveable { mutableStateOf(false) }
                var feedbackMessage by rememberSaveable { mutableStateOf<String?>(null) }
                var isSuccess by rememberSaveable { mutableStateOf(false) }

                RegistrationScreen(
                    onRegister = { name, email, password ->
                        if (password.length < 6) {
                            isSuccess = false
                            feedbackMessage = "A senha precisa ter pelo menos 6 caracteres."
                        } else {
                            isLoading = true
                            feedbackMessage = null

                            authRepository.registerUser(
                                email = email,
                                password = password,
                                onSuccess = { uid ->
                                    userRepository.saveUserProfile(
                                        uid = uid,
                                        name = name.trim(),
                                        email = email.trim(),
                                        onSuccess = {
                                            isLoading = false
                                            isSuccess = true
                                            feedbackMessage = "Cadastro realizado com sucesso!"
                                        },
                                        onError = { _ ->
                                            isLoading = false
                                            isSuccess = false
                                            feedbackMessage = "Sua conta foi criada, mas não foi possível salvar o perfil."
                                        }
                                    )
                                },
                                onError = { error ->
                                    isLoading = false
                                    isSuccess = false
                                    feedbackMessage = error.toUserMessage()
                                }
                            )
                        }
                    },
                    onBack = {},
                    isLoading = isLoading,
                    feedbackMessage = feedbackMessage,
                    isSuccess = isSuccess
                )
            }
        }
    }
}

private fun RegistrationError.toUserMessage(): String = when (this) {
    RegistrationError.INVALID_EMAIL -> "O e-mail informado é inválido."
    RegistrationError.WEAK_PASSWORD -> "A senha precisa ter pelo menos 6 caracteres."
    RegistrationError.EMAIL_ALREADY_IN_USE -> "Já existe uma conta com esse e-mail."
    RegistrationError.UNKNOWN -> "Não foi possível realizar o cadastro. Tente novamente."
}
