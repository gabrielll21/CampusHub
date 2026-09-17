package br.com.uri.meuprojeto.navigation

import android.util.Patterns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.uri.meuprojeto.auth.AuthRepository
import br.com.uri.meuprojeto.auth.LoginError
import br.com.uri.meuprojeto.auth.RegistrationError
import br.com.uri.meuprojeto.ui.DashboardScreen
import br.com.uri.meuprojeto.ui.LoginScreen
import br.com.uri.meuprojeto.ui.RegistrationScreen
import br.com.uri.meuprojeto.user.UserRepository

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    userRepository: UserRepository
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LOGIN_ROUTE
    ) {
        composable(LOGIN_ROUTE) {
            var isLoading by rememberSaveable { mutableStateOf(false) }
            var feedbackMessage by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            LoginScreen(
                onLogin = { email, password ->
                    if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                        feedbackMessage = "Digite um e-mail válido."
                    } else {
                        isLoading = true
                        feedbackMessage = null

                        authRepository.loginUser(
                            email = email,
                            password = password,
                            onSuccess = {
                                isLoading = false
                                navController.navigate(DASHBOARD_ROUTE) {
                                    popUpTo(LOGIN_ROUTE) {
                                        inclusive = true
                                    }
                                }
                            },
                            onError = { error ->
                                isLoading = false
                                feedbackMessage = error.toUserMessage()
                            }
                        )
                    }
                },
                onCreateAccount = {
                    navController.navigate(REGISTRATION_ROUTE)
                },
                isLoading = isLoading,
                feedbackMessage = feedbackMessage
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

        composable(DASHBOARD_ROUTE) {
            var name by rememberSaveable { mutableStateOf<String?>(null) }
            var email by rememberSaveable { mutableStateOf<String?>(null) }
            var isLoading by rememberSaveable { mutableStateOf(true) }
            var errorMessage by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            LaunchedEffect(Unit) {
                isLoading = true
                errorMessage = null

                val uid = authRepository.getCurrentUserUid()
                if (uid == null) {
                    isLoading = false
                    errorMessage =
                        "Não foi possível identificar o usuário autenticado."
                } else {
                    userRepository.getUserProfile(
                        uid = uid,
                        onSuccess = { profile ->
                            name = profile.name
                            email = profile.email
                            isLoading = false
                        },
                        onError = {
                            isLoading = false
                            errorMessage =
                                "Não foi possível carregar seu perfil."
                        }
                    )
                }
            }

            DashboardScreen(
                name = name,
                email = email,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onLogout = {
                    authRepository.signOut()
                    navController.navigate(LOGIN_ROUTE) {
                        popUpTo(DASHBOARD_ROUTE) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
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
private const val DASHBOARD_ROUTE = "dashboard"
