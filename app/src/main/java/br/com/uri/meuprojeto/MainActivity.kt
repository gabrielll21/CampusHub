package br.com.uri.meuprojeto

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.uri.meuprojeto.auth.AuthRepository
import br.com.uri.meuprojeto.auth.LoginError
import br.com.uri.meuprojeto.auth.RegistrationError
import br.com.uri.meuprojeto.ui.LoginScreen
import br.com.uri.meuprojeto.ui.RegistrationScreen
import br.com.uri.meuprojeto.ui.theme.MeuProjetoTheme
import br.com.uri.meuprojeto.user.UserRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeuProjetoTheme {
                val navController = rememberNavController()
                val authRepository = remember { AuthRepository() }
                val userRepository = remember { UserRepository() }

                NavHost(
                    navController = navController,
                    startDestination = LOGIN_ROUTE
                ) {
                    composable(LOGIN_ROUTE) {
                        var isLoading by rememberSaveable { mutableStateOf(false) }
                        var feedbackMessage by rememberSaveable {
                            mutableStateOf<String?>(null)
                        }
                        var isSuccess by rememberSaveable { mutableStateOf(false) }

                        val onLoginSuccess: () -> Unit = {
                            isLoading = false
                            isSuccess = true
                            feedbackMessage = "Login realizado com sucesso!"
                        }

                        LoginScreen(
                            onLogin = { email, password ->
                                if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                                    isSuccess = false
                                    feedbackMessage = "Digite um e-mail válido."
                                } else {
                                    isLoading = true
                                    feedbackMessage = null

                                    authRepository.loginUser(
                                        email = email,
                                        password = password,
                                        onSuccess = onLoginSuccess,
                                        onError = { error ->
                                            isLoading = false
                                            isSuccess = false
                                            feedbackMessage = error.toUserMessage()
                                        }
                                    )
                                }
                            },
                            onCreateAccount = {
                                navController.navigate(REGISTRATION_ROUTE)
                            },
                            isLoading = isLoading,
                            feedbackMessage = feedbackMessage,
                            isSuccess = isSuccess
                        )
                    }

                    composable(REGISTRATION_ROUTE) {
                        var isLoading by rememberSaveable { mutableStateOf(false) }
                        var feedbackMessage by rememberSaveable {
                            mutableStateOf<String?>(null)
                        }
                        var isSuccess by rememberSaveable { mutableStateOf(false) }

                        RegistrationScreen(
                            onRegister = { name, email, password ->
                                if (password.length < 6) {
                                    isSuccess = false
                                    feedbackMessage =
                                        "A senha precisa ter pelo menos 6 caracteres."
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
                                                    feedbackMessage =
                                                        "Cadastro realizado com sucesso!"
                                                },
                                                onError = { _ ->
                                                    isLoading = false
                                                    isSuccess = false
                                                    feedbackMessage =
                                                        "Sua conta foi criada, mas não foi possível salvar o perfil."
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
                            onBack = {
                                navController.popBackStack()
                            },
                            isLoading = isLoading,
                            feedbackMessage = feedbackMessage,
                            isSuccess = isSuccess
                        )
                    }
                }
            }
        }
    }
}

private fun LoginError.toUserMessage(): String = when (this) {
    LoginError.INVALID_CREDENTIALS -> "E-mail ou senha incorretos."
    LoginError.UNKNOWN -> "Não foi possível realizar o login. Tente novamente."
}

private fun RegistrationError.toUserMessage(): String = when (this) {
    RegistrationError.INVALID_EMAIL -> "O e-mail informado é inválido."
    RegistrationError.WEAK_PASSWORD -> "A senha precisa ter pelo menos 6 caracteres."
    RegistrationError.EMAIL_ALREADY_IN_USE -> "Já existe uma conta com esse e-mail."
    RegistrationError.UNKNOWN -> "Não foi possível realizar o cadastro. Tente novamente."
}

private const val LOGIN_ROUTE = "login"
private const val REGISTRATION_ROUTE = "registration"
