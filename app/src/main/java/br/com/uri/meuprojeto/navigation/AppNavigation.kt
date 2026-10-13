package br.com.uri.meuprojeto.navigation

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.uri.meuprojeto.auth.AuthRepository
import br.com.uri.meuprojeto.auth.LoginError
import br.com.uri.meuprojeto.auth.RegistrationError
import br.com.uri.meuprojeto.comment.Comment
import br.com.uri.meuprojeto.comment.CommentRepository
import br.com.uri.meuprojeto.event.Event
import br.com.uri.meuprojeto.event.EventRepository
import br.com.uri.meuprojeto.favorite.FavoriteRepository
import br.com.uri.meuprojeto.registration.Registration
import br.com.uri.meuprojeto.registration.RegistrationRepository
import br.com.uri.meuprojeto.ui.DashboardScreen
import br.com.uri.meuprojeto.ui.EventDetailsScreen
import br.com.uri.meuprojeto.ui.EventsScreen
import br.com.uri.meuprojeto.ui.ForgotPasswordScreen
import br.com.uri.meuprojeto.ui.LoginScreen
import br.com.uri.meuprojeto.ui.MyFavoritesScreen
import br.com.uri.meuprojeto.ui.MyEventsScreen
import br.com.uri.meuprojeto.ui.ProfileScreen
import br.com.uri.meuprojeto.ui.RegistrationScreen
import br.com.uri.meuprojeto.user.UserRepository

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    userRepository: UserRepository
) {
    val navController = rememberNavController()
    val eventRepository = remember { EventRepository() }
    val registrationRepository = remember { RegistrationRepository() }
    val favoriteRepository = remember { FavoriteRepository() }
    val commentRepository = remember { CommentRepository() }
    var currentUserName by rememberSaveable { mutableStateOf<String?>(null) }
    var currentUserEmail by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordResetEmail by rememberSaveable { mutableStateOf("") }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var favoritesRefreshKey by rememberSaveable { mutableStateOf(0) }

    val logout: () -> Unit = {
        authRepository.signOut()
        currentUserName = null
        currentUserEmail = null
        selectedEvent = null
        navController.navigate(LOGIN_ROUTE) {
            popUpTo(DASHBOARD_ROUTE) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

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
                onForgotPassword = { email ->
                    passwordResetEmail = email
                    navController.navigate(FORGOT_PASSWORD_ROUTE) {
                        launchSingleTop = true
                    }
                },
                isLoading = isLoading,
                feedbackMessage = feedbackMessage
            )
        }

        composable(FORGOT_PASSWORD_ROUTE) {
            ForgotPasswordScreen(
                initialEmail = passwordResetEmail,
                onSendResetEmail = { email, onSuccess, onError ->
                    authRepository.sendPasswordResetEmail(
                        email = email,
                        onSuccess = onSuccess,
                        onError = onError
                    )
                },
                onBack = { navController.popBackStack() }
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
                            currentUserName = profile.name
                            currentUserEmail = profile.email
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
                name = currentUserName,
                email = currentUserEmail,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onLogout = logout,
                onEventsClick = {
                    navController.navigate(EVENTS_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onMyEventsClick = {
                    navController.navigate(MY_EVENTS_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onMyFavoritesClick = {
                    navController.navigate(MY_FAVORITES_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate(PROFILE_ROUTE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(EVENTS_ROUTE) {
            var events by remember { mutableStateOf(emptyList<Event>()) }
            var isLoading by rememberSaveable { mutableStateOf(true) }
            var errorMessage by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            fun loadEvents() {
                isLoading = true
                errorMessage = null

                eventRepository.getEvents(
                    onSuccess = { loadedEvents ->
                        events = loadedEvents
                        isLoading = false
                    },
                    onError = {
                        isLoading = false
                        errorMessage =
                            "Não foi possível carregar os eventos. Tente novamente."
                    }
                )
            }

            LaunchedEffect(Unit) {
                loadEvents()
            }

            EventsScreen(
                events = events,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onRetry = { loadEvents() },
                onEventClick = { event ->
                    selectedEvent = event
                    navController.navigate(EVENT_DETAILS_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(EVENT_DETAILS_ROUTE) {
            selectedEvent?.let { event ->
                var registration by remember(event.id) {
                    mutableStateOf<Registration?>(null)
                }
                var isRegistrationLoading by remember(event.id) {
                    mutableStateOf(true)
                }
                var registrationMessage by remember(event.id) {
                    mutableStateOf<String?>(null)
                }
                var isRegistrationMessageError by remember(event.id) {
                    mutableStateOf(false)
                }
                val userId = authRepository.getCurrentUserUid()
                var isFavorite by remember(event.id, userId) {
                    mutableStateOf<Boolean?>(null)
                }
                var isFavoriteLoading by remember(event.id, userId) {
                    mutableStateOf(true)
                }
                var favoriteMessage by remember(event.id, userId) {
                    mutableStateOf<String?>(null)
                }
                var isFavoriteMessageError by remember(event.id, userId) {
                    mutableStateOf(false)
                }
                var comments by remember(event.id) {
                    mutableStateOf(emptyList<Comment>())
                }
                var isCommentsLoading by remember(event.id) {
                    mutableStateOf(true)
                }
                var commentsErrorMessage by remember(event.id) {
                    mutableStateOf<String?>(null)
                }
                var commentContent by remember(event.id) {
                    mutableStateOf("")
                }
                var isCommentSubmitting by remember(event.id) {
                    mutableStateOf(false)
                }
                var commentSubmissionMessage by remember(event.id) {
                    mutableStateOf<String?>(null)
                }
                var isCommentSubmissionError by remember(event.id) {
                    mutableStateOf(false)
                }
                var editingCommentId by remember(event.id) {
                    mutableStateOf<String?>(null)
                }
                var editingCommentContent by remember(event.id) {
                    mutableStateOf("")
                }
                var deletingCommentId by remember(event.id) {
                    mutableStateOf<String?>(null)
                }
                var commentMutationLoadingId by remember(event.id) {
                    mutableStateOf<String?>(null)
                }
                var commentMutationMessage by remember(event.id) {
                    mutableStateOf<String?>(null)
                }
                var isCommentMutationMessageError by remember(event.id) {
                    mutableStateOf(false)
                }

                fun loadComments() {
                    if (event.id.isBlank()) {
                        isCommentsLoading = false
                        commentsErrorMessage =
                            "Não foi possível identificar o evento."
                        return
                    }

                    isCommentsLoading = true
                    commentsErrorMessage = null

                    commentRepository.getComments(
                        eventId = event.id,
                        onSuccess = { loadedComments ->
                            comments = loadedComments
                            isCommentsLoading = false
                        },
                        onError = {
                            isCommentsLoading = false
                            commentsErrorMessage =
                                "Não foi possível carregar os comentários."
                        }
                    )
                }

                LaunchedEffect(event.id, userId) {
                    if (userId == null) {
                        isRegistrationLoading = false
                        isRegistrationMessageError = true
                        registrationMessage =
                            "Não foi possível identificar o usuário autenticado."
                    } else {
                        isRegistrationLoading = true
                        registrationRepository.getRegistration(
                            userId = userId,
                            eventId = event.id,
                            onSuccess = { currentRegistration ->
                                registration = currentRegistration
                                isRegistrationLoading = false
                            },
                            onError = {
                                isRegistrationLoading = false
                                isRegistrationMessageError = true
                                registrationMessage =
                                    "Não foi possível consultar sua inscrição."
                            }
                        )
                    }
                }

                LaunchedEffect(event.id) {
                    loadComments()
                }

                LaunchedEffect(event.id, userId) {
                    isFavorite = null
                    favoriteMessage = null

                    if (userId == null) {
                        isFavoriteLoading = false
                        isFavoriteMessageError = true
                        favoriteMessage =
                            "Não foi possível identificar o usuário autenticado."
                    } else if (event.id.isBlank()) {
                        isFavoriteLoading = false
                        isFavoriteMessageError = true
                        favoriteMessage = "Não foi possível identificar o evento."
                    } else {
                        isFavoriteLoading = true
                        favoriteRepository.isFavorite(
                            userId = userId,
                            eventId = event.id,
                            onSuccess = { favoriteExists ->
                                isFavorite = favoriteExists
                                isFavoriteLoading = false
                            },
                            onError = {
                                isFavoriteLoading = false
                                isFavoriteMessageError = true
                                favoriteMessage =
                                    "Não foi possível consultar o favorito."
                            }
                        )
                    }
                }

                EventDetailsScreen(
                    event = event,
                    isRegistered = registration != null,
                    isRegistrationLoading = isRegistrationLoading,
                    registrationMessage = registrationMessage,
                    isRegistrationMessageError = isRegistrationMessageError,
                    isFavorite = isFavorite,
                    isFavoriteLoading = isFavoriteLoading,
                    favoriteMessage = favoriteMessage,
                    isFavoriteMessageError = isFavoriteMessageError,
                    comments = comments,
                    isCommentsLoading = isCommentsLoading,
                    commentsErrorMessage = commentsErrorMessage,
                    commentContent = commentContent,
                    isCommentSubmitting = isCommentSubmitting,
                    commentSubmissionMessage = commentSubmissionMessage,
                    isCommentSubmissionError = isCommentSubmissionError,
                    currentUserId = userId,
                    editingCommentId = editingCommentId,
                    editingCommentContent = editingCommentContent,
                    deletingCommentId = deletingCommentId,
                    commentMutationLoadingId = commentMutationLoadingId,
                    commentMutationMessage = commentMutationMessage,
                    isCommentMutationMessageError = isCommentMutationMessageError,
                    onRegister = {
                        if (!isRegistrationLoading && userId != null) {
                            isRegistrationLoading = true
                            registrationMessage = null
                            registrationRepository.createRegistration(
                                userId = userId,
                                eventId = event.id,
                                onSuccess = { createdRegistration ->
                                    registration = createdRegistration
                                    isRegistrationLoading = false
                                    isRegistrationMessageError = false
                                    registrationMessage =
                                        "Inscrição realizada com sucesso!"
                                },
                                onError = {
                                    isRegistrationLoading = false
                                    isRegistrationMessageError = true
                                    registrationMessage =
                                        "Não foi possível realizar a inscrição."
                                }
                            )
                        }
                    },
                    onCancelRegistration = {
                        val currentRegistration = registration
                        if (!isRegistrationLoading && currentRegistration != null) {
                            isRegistrationLoading = true
                            registrationMessage = null
                            registrationRepository.deleteRegistration(
                                registrationId = currentRegistration.id,
                                onSuccess = {
                                    registration = null
                                    isRegistrationLoading = false
                                    isRegistrationMessageError = false
                                    registrationMessage =
                                        "Inscrição cancelada com sucesso."
                                },
                                onError = {
                                    isRegistrationLoading = false
                                    isRegistrationMessageError = true
                                    registrationMessage =
                                        "Não foi possível cancelar a inscrição."
                                }
                            )
                        }
                    },
                    onToggleFavorite = {
                        val currentUserId = userId
                        val currentFavorite = isFavorite
                        if (
                            !isFavoriteLoading &&
                            currentUserId != null &&
                            event.id.isNotBlank() &&
                            currentFavorite != null
                        ) {
                            isFavoriteLoading = true
                            favoriteMessage = null
                            isFavoriteMessageError = false

                            if (currentFavorite) {
                                favoriteRepository.removeFavorite(
                                    userId = currentUserId,
                                    eventId = event.id,
                                    onSuccess = {
                                        isFavorite = false
                                        isFavoriteLoading = false
                                        favoritesRefreshKey++
                                        favoriteMessage =
                                            "Evento removido dos favoritos."
                                    },
                                    onError = {
                                        isFavoriteLoading = false
                                        isFavoriteMessageError = true
                                        favoriteMessage =
                                            "Não foi possível remover o favorito."
                                    }
                                )
                            } else {
                                favoriteRepository.addFavorite(
                                    userId = currentUserId,
                                    eventId = event.id,
                                    onSuccess = {
                                        isFavorite = true
                                        isFavoriteLoading = false
                                        favoritesRefreshKey++
                                        favoriteMessage =
                                            "Evento adicionado aos favoritos."
                                    },
                                    onError = {
                                        isFavoriteLoading = false
                                        isFavoriteMessageError = true
                                        favoriteMessage =
                                            "Não foi possível adicionar o favorito."
                                    }
                                )
                            }
                        }
                    },
                    onCommentContentChange = { content ->
                        commentContent = content
                        commentSubmissionMessage = null
                        isCommentSubmissionError = false
                    },
                    onRetryComments = { loadComments() },
                    onSubmitComment = {
                        val normalizedContent = commentContent.trim()
                        val currentUserId = userId
                        val normalizedAuthorName = currentUserName?.trim()

                        when {
                            event.id.isBlank() -> {
                                isCommentSubmissionError = true
                                commentSubmissionMessage =
                                    "Não foi possível identificar o evento."
                            }

                            currentUserId.isNullOrBlank() -> {
                                isCommentSubmissionError = true
                                commentSubmissionMessage =
                                    "Não foi possível identificar o usuário autenticado."
                            }

                            normalizedAuthorName.isNullOrBlank() -> {
                                isCommentSubmissionError = true
                                commentSubmissionMessage =
                                    "Não foi possível carregar seu nome de usuário."
                            }

                            normalizedContent.isEmpty() -> {
                                isCommentSubmissionError = true
                                commentSubmissionMessage =
                                    "Escreva um comentário antes de publicar."
                            }

                            else -> {
                                isCommentSubmitting = true
                                commentSubmissionMessage = null
                                isCommentSubmissionError = false

                                commentRepository.addComment(
                                    eventId = event.id,
                                    authorId = currentUserId,
                                    authorName = normalizedAuthorName,
                                    content = normalizedContent,
                                    onSuccess = {
                                        commentContent = ""
                                        isCommentSubmitting = false
                                        isCommentSubmissionError = false
                                        commentSubmissionMessage =
                                            "Comentário publicado com sucesso."
                                        loadComments()
                                    },
                                    onError = {
                                        isCommentSubmitting = false
                                        isCommentSubmissionError = true
                                        commentSubmissionMessage =
                                            "Não foi possível publicar o comentário."
                                    }
                                )
                            }
                        }
                    },
                    onStartEditComment = { comment ->
                        if (
                            comment.authorId == userId &&
                            commentMutationLoadingId == null &&
                            editingCommentId == null &&
                            deletingCommentId == null
                        ) {
                            editingCommentId = comment.id
                            editingCommentContent = comment.content
                            deletingCommentId = null
                            commentMutationMessage = null
                            isCommentMutationMessageError = false
                        }
                    },
                    onEditCommentContentChange = { content ->
                        editingCommentContent = content
                        commentMutationMessage = null
                        isCommentMutationMessageError = false
                    },
                    onCancelEditComment = {
                        if (commentMutationLoadingId == null) {
                            editingCommentId = null
                            editingCommentContent = ""
                            commentMutationMessage = null
                            isCommentMutationMessageError = false
                        }
                    },
                    onSaveEditComment = {
                        val commentId = editingCommentId
                        val currentUserId = userId
                        val normalizedContent = editingCommentContent.trim()

                        when {
                            commentId.isNullOrBlank() -> {
                                isCommentMutationMessageError = true
                                commentMutationMessage =
                                    "Não foi possível identificar o comentário."
                            }

                            currentUserId.isNullOrBlank() -> {
                                isCommentMutationMessageError = true
                                commentMutationMessage =
                                    "Não foi possível identificar o usuário autenticado."
                            }

                            normalizedContent.isEmpty() -> {
                                isCommentMutationMessageError = true
                                commentMutationMessage =
                                    "O comentário não pode ficar vazio."
                            }

                            commentMutationLoadingId == null -> {
                                commentMutationLoadingId = commentId
                                commentMutationMessage = null
                                isCommentMutationMessageError = false

                                commentRepository.updateComment(
                                    eventId = event.id,
                                    commentId = commentId,
                                    authorId = currentUserId,
                                    content = normalizedContent,
                                    onSuccess = {
                                        editingCommentId = null
                                        editingCommentContent = ""
                                        commentMutationLoadingId = null
                                        isCommentMutationMessageError = false
                                        commentMutationMessage =
                                            "Comentário atualizado com sucesso."
                                        loadComments()
                                    },
                                    onError = {
                                        commentMutationLoadingId = null
                                        isCommentMutationMessageError = true
                                        commentMutationMessage =
                                            "Não foi possível atualizar o comentário."
                                    }
                                )
                            }
                        }
                    },
                    onRequestDeleteComment = { comment ->
                        if (
                            comment.authorId == userId &&
                            commentMutationLoadingId == null &&
                            editingCommentId == null &&
                            deletingCommentId == null
                        ) {
                            deletingCommentId = comment.id
                            editingCommentId = null
                            editingCommentContent = ""
                            commentMutationMessage = null
                            isCommentMutationMessageError = false
                        }
                    },
                    onCancelDeleteComment = {
                        if (commentMutationLoadingId == null) {
                            deletingCommentId = null
                        }
                    },
                    onConfirmDeleteComment = {
                        val commentId = deletingCommentId
                        val currentUserId = userId

                        when {
                            commentId.isNullOrBlank() -> {
                                deletingCommentId = null
                            }

                            currentUserId.isNullOrBlank() -> {
                                deletingCommentId = null
                                isCommentMutationMessageError = true
                                commentMutationMessage =
                                    "Não foi possível identificar o usuário autenticado."
                            }

                            commentMutationLoadingId == null -> {
                                commentMutationLoadingId = commentId
                                commentMutationMessage = null
                                isCommentMutationMessageError = false

                                commentRepository.deleteComment(
                                    eventId = event.id,
                                    commentId = commentId,
                                    authorId = currentUserId,
                                    onSuccess = {
                                        deletingCommentId = null
                                        commentMutationLoadingId = null
                                        isCommentMutationMessageError = false
                                        commentMutationMessage =
                                            "Comentário excluído com sucesso."
                                        loadComments()
                                    },
                                    onError = {
                                        deletingCommentId = null
                                        commentMutationLoadingId = null
                                        isCommentMutationMessageError = true
                                        commentMutationMessage =
                                            "Não foi possível excluir o comentário."
                                    }
                                )
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(MY_EVENTS_ROUTE) {
            var events by remember { mutableStateOf(emptyList<Event>()) }
            var isLoading by rememberSaveable { mutableStateOf(true) }
            var errorMessage by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            fun loadMyEvents() {
                isLoading = true
                errorMessage = null
                events = emptyList()

                val userId = authRepository.getCurrentUserUid()
                if (userId == null) {
                    isLoading = false
                    errorMessage =
                        "Não foi possível identificar o usuário autenticado."
                    return
                }

                registrationRepository.getRegistrations(
                    userId = userId,
                    onSuccess = { registrations ->
                        if (registrations.isEmpty()) {
                            events = emptyList()
                            isLoading = false
                        } else {
                            val registeredEventIds =
                                registrations.map { it.eventId }.toSet()

                            eventRepository.getEvents(
                                onSuccess = { availableEvents ->
                                    events = availableEvents.filter { event ->
                                        event.id in registeredEventIds
                                    }
                                    isLoading = false
                                },
                                onError = {
                                    isLoading = false
                                    errorMessage =
                                        "Não foi possível carregar seus eventos."
                                }
                            )
                        }
                    },
                    onError = {
                        isLoading = false
                        errorMessage =
                            "Não foi possível carregar suas inscrições."
                    }
                )
            }

            LaunchedEffect(Unit) {
                loadMyEvents()
            }

            MyEventsScreen(
                events = events,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onEventClick = { event ->
                    selectedEvent = event
                    navController.navigate(EVENT_DETAILS_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onRetry = { loadMyEvents() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(MY_FAVORITES_ROUTE) {
            var events by remember { mutableStateOf(emptyList<Event>()) }
            var isLoading by rememberSaveable { mutableStateOf(true) }
            var errorMessage by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            fun loadMyFavorites() {
                isLoading = true
                errorMessage = null
                events = emptyList()

                val userId = authRepository.getCurrentUserUid()
                if (userId == null) {
                    isLoading = false
                    errorMessage =
                        "Não foi possível identificar o usuário autenticado."
                    return
                }

                favoriteRepository.getFavoriteEventIds(
                    userId = userId,
                    onSuccess = { favoriteEventIds ->
                        if (favoriteEventIds.isEmpty()) {
                            events = emptyList()
                            isLoading = false
                        } else {
                            eventRepository.getEvents(
                                onSuccess = { availableEvents ->
                                    events = availableEvents.filter { event ->
                                        event.id in favoriteEventIds
                                    }
                                    isLoading = false
                                },
                                onError = {
                                    isLoading = false
                                    errorMessage =
                                        "Não foi possível carregar seus favoritos."
                                }
                            )
                        }
                    },
                    onError = {
                        isLoading = false
                        errorMessage =
                            "Não foi possível carregar seus favoritos."
                    }
                )
            }

            LaunchedEffect(favoritesRefreshKey) {
                loadMyFavorites()
            }

            MyFavoritesScreen(
                events = events,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onEventClick = { event ->
                    selectedEvent = event
                    navController.navigate(EVENT_DETAILS_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onRetry = { loadMyFavorites() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(PROFILE_ROUTE) {
            var uid by rememberSaveable { mutableStateOf<String?>(null) }
            var isLoading by rememberSaveable { mutableStateOf(true) }
            var errorMessage by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            LaunchedEffect(Unit) {
                isLoading = true
                errorMessage = null

                val currentUid = authRepository.getCurrentUserUid()
                if (currentUid == null) {
                    isLoading = false
                    errorMessage =
                        "Não foi possível identificar o usuário autenticado."
                } else {
                    uid = currentUid
                    userRepository.getUserProfile(
                        uid = currentUid,
                        onSuccess = { profile ->
                            currentUserName = profile.name
                            currentUserEmail = profile.email
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

            val profileUid = uid
            val profileName = currentUserName
            val profileEmail = currentUserEmail

            when {
                isLoading -> ProfileLoadingState()
                errorMessage != null -> ProfileErrorState(
                    message = errorMessage.orEmpty(),
                    onBack = { navController.popBackStack() }
                )
                profileUid != null &&
                    profileName != null &&
                    profileEmail != null -> ProfileScreen(
                        uid = profileUid,
                        name = profileName,
                        email = profileEmail,
                        onSaveName = { userUid, newName, onSuccess, onError ->
                            userRepository.updateUserName(
                                uid = userUid,
                                name = newName,
                                onSuccess = {
                                    currentUserName = newName
                                    onSuccess()
                                },
                                onError = onError
                            )
                        },
                        onBack = {
                            navController.popBackStack()
                        },
                        onLogout = logout
                    )
            }
        }
    }
}

@Composable
private fun ProfileLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ProfileErrorState(
    message: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card {
            Text(
                text = message,
                modifier = Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Button(
            onClick = onBack,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Voltar")
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
private const val FORGOT_PASSWORD_ROUTE = "forgot_password"
private const val REGISTRATION_ROUTE = "registration"
private const val DASHBOARD_ROUTE = "dashboard"
private const val EVENTS_ROUTE = "events"
private const val EVENT_DETAILS_ROUTE = "event_details"
private const val MY_EVENTS_ROUTE = "my_events"
private const val MY_FAVORITES_ROUTE = "my_favorites"
private const val PROFILE_ROUTE = "profile"
