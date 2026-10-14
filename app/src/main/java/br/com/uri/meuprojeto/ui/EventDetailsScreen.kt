package br.com.uri.meuprojeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.uri.meuprojeto.comment.Comment
import br.com.uri.meuprojeto.event.Event
import br.com.uri.meuprojeto.event.EventStatus
import br.com.uri.meuprojeto.rating.Rating
import br.com.uri.meuprojeto.rating.calculateAverageRating
import br.com.uri.meuprojeto.ui.components.CampusHubButton
import br.com.uri.meuprojeto.ui.components.CampusHubCard
import br.com.uri.meuprojeto.ui.components.CampusHubEmptyState
import br.com.uri.meuprojeto.ui.components.CampusHubErrorState
import br.com.uri.meuprojeto.ui.components.CampusHubLoadingState
import br.com.uri.meuprojeto.ui.components.CampusHubOutlinedButton
import br.com.uri.meuprojeto.ui.components.CampusHubSectionHeader
import br.com.uri.meuprojeto.ui.components.CampusHubTextField
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val COMMENT_TIME_ZONE = ZoneId.of("America/Sao_Paulo")
private val COMMENT_DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm", Locale.ROOT)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    event: Event,
    isRegistered: Boolean,
    isRegistrationLoading: Boolean,
    registrationMessage: String?,
    isRegistrationMessageError: Boolean,
    isFavorite: Boolean?,
    isFavoriteLoading: Boolean,
    favoriteMessage: String?,
    isFavoriteMessageError: Boolean,
    comments: List<Comment>,
    isCommentsLoading: Boolean,
    commentsErrorMessage: String?,
    commentContent: String,
    isCommentSubmitting: Boolean,
    commentSubmissionMessage: String?,
    isCommentSubmissionError: Boolean,
    currentUserId: String?,
    editingCommentId: String?,
    editingCommentContent: String,
    deletingCommentId: String?,
    commentMutationLoadingId: String?,
    commentMutationMessage: String?,
    isCommentMutationMessageError: Boolean,
    ratings: List<Rating>,
    isRatingsLoading: Boolean,
    ratingsErrorMessage: String?,
    userRating: Rating?,
    isUserRatingLoading: Boolean,
    userRatingErrorMessage: String?,
    selectedRatingScore: Int?,
    isRatingMutationLoading: Boolean,
    ratingMessage: String?,
    isRatingMessageError: Boolean,
    eventStatus: EventStatus,
    canRate: Boolean,
    onRegister: () -> Unit,
    onCancelRegistration: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCommentContentChange: (String) -> Unit,
    onRetryComments: () -> Unit,
    onSubmitComment: () -> Unit,
    onStartEditComment: (Comment) -> Unit,
    onEditCommentContentChange: (String) -> Unit,
    onCancelEditComment: () -> Unit,
    onSaveEditComment: () -> Unit,
    onRequestDeleteComment: (Comment) -> Unit,
    onCancelDeleteComment: () -> Unit,
    onConfirmDeleteComment: () -> Unit,
    onRetryRatings: () -> Unit,
    onRatingScoreSelected: (Int) -> Unit,
    onSubmitRating: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do evento") },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Voltar"
                        }
                    ) {
                        Text("Voltar")
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(contentPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CampusHubCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    EventInformation(label = "Data", value = event.date)
                    Spacer(modifier = Modifier.height(12.dp))
                    EventInformation(label = "Localização", value = event.location)
                    event.category
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { category ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    ),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Sobre o evento",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                CampusHubSectionHeader(
                    title = "Sua participação",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                CampusHubCard(modifier = Modifier.fillMaxWidth()) {
                    if (isRegistrationLoading) {
                        CampusHubLoadingState(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else if (isRegistered) {
                        CampusHubOutlinedButton(
                            text = "Cancelar inscrição",
                            onClick = onCancelRegistration,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        CampusHubButton(
                            text = "Inscrever-se",
                            onClick = onRegister,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (isFavoriteLoading) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CampusHubLoadingState(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else if (isFavorite != null && event.id.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CampusHubOutlinedButton(
                            text = if (isFavorite) {
                                "Remover dos favoritos"
                            } else {
                                "Adicionar aos favoritos"
                            },
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = if (isFavorite) {
                                        "Remover dos favoritos"
                                    } else {
                                        "Adicionar aos favoritos"
                                    }
                                }
                        )
                    }

                    registrationMessage?.let { message ->
                        StatusMessage(message = message, isError = isRegistrationMessageError)
                    }
                    favoriteMessage?.let { message ->
                        StatusMessage(message = message, isError = isFavoriteMessageError)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                RatingSection(
                    ratings = ratings,
                    isRatingsLoading = isRatingsLoading,
                    ratingsErrorMessage = ratingsErrorMessage,
                    userRating = userRating,
                    isUserRatingLoading = isUserRatingLoading,
                    userRatingErrorMessage = userRatingErrorMessage,
                    selectedRatingScore = selectedRatingScore,
                    isRatingMutationLoading = isRatingMutationLoading,
                    ratingMessage = ratingMessage,
                    isRatingMessageError = isRatingMessageError,
                    eventStatus = eventStatus,
                    canRate = canRate,
                    onRetryRatings = onRetryRatings,
                    onRatingScoreSelected = onRatingScoreSelected,
                    onSubmitRating = onSubmitRating
                )

                Spacer(modifier = Modifier.height(28.dp))
                CampusHubSectionHeader(
                    title = "Comentários",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isCommentsLoading -> CampusHubLoadingState(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    commentsErrorMessage != null -> CampusHubCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CampusHubErrorState(
                            message = commentsErrorMessage,
                            onRetry = onRetryComments,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    comments.isEmpty() -> CampusHubEmptyState(
                        message = "Seja o primeiro a comentar.",
                        modifier = Modifier.fillMaxWidth()
                    )
                    else -> Column(modifier = Modifier.fillMaxWidth()) {
                        comments.forEach { comment ->
                            CommentCard(
                                comment = comment,
                                isOwnComment = comment.authorId == currentUserId,
                                isEditing = comment.id == editingCommentId,
                                isMutating = comment.id == commentMutationLoadingId,
                                editingCommentContent = editingCommentContent,
                                editingCommentId = editingCommentId,
                                deletingCommentId = deletingCommentId,
                                commentMutationLoadingId = commentMutationLoadingId,
                                onStartEditComment = onStartEditComment,
                                onEditCommentContentChange = onEditCommentContentChange,
                                onCancelEditComment = onCancelEditComment,
                                onSaveEditComment = onSaveEditComment,
                                onRequestDeleteComment = onRequestDeleteComment
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                CampusHubCard(modifier = Modifier.fillMaxWidth()) {
                    CampusHubTextField(
                        value = commentContent,
                        onValueChange = onCommentContentChange,
                        label = "Novo comentário",
                        enabled = !isCommentSubmitting,
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CampusHubButton(
                        text = "Comentar",
                        onClick = onSubmitComment,
                        enabled = commentContent.trim().isNotEmpty(),
                        isLoading = isCommentSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    )
                    commentSubmissionMessage?.let { message ->
                        StatusMessage(message = message, isError = isCommentSubmissionError)
                    }
                    commentMutationMessage?.let { message ->
                        StatusMessage(
                            message = message,
                            isError = isCommentMutationMessageError
                        )
                    }
                }
            }
        }
    }

    if (deletingCommentId != null) {
        AlertDialog(
            onDismissRequest = {
                if (commentMutationLoadingId == null) {
                    onCancelDeleteComment()
                }
            },
            title = { Text("Excluir comentário?") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDeleteComment,
                    enabled = commentMutationLoadingId == null
                ) {
                    if (commentMutationLoadingId != null) {
                        CampusHubLoadingState(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Excluir")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancelDeleteComment,
                    enabled = commentMutationLoadingId == null
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CommentCard(
    comment: Comment,
    isOwnComment: Boolean,
    isEditing: Boolean,
    isMutating: Boolean,
    editingCommentContent: String,
    editingCommentId: String?,
    deletingCommentId: String?,
    commentMutationLoadingId: String?,
    onStartEditComment: (Comment) -> Unit,
    onEditCommentContentChange: (String) -> Unit,
    onCancelEditComment: () -> Unit,
    onSaveEditComment: () -> Unit,
    onRequestDeleteComment: (Comment) -> Unit
) {
    CampusHubCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = comment.authorName, style = MaterialTheme.typography.titleMedium)
        Text(
            text = formatCommentDate(comment.createdAt),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium
        )
        if (comment.updatedAt != null) {
            Text(
                text = "Editado em ${formatCommentDate(comment.updatedAt)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }

        if (isOwnComment && !isEditing) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { onStartEditComment(comment) },
                    enabled = commentMutationLoadingId == null &&
                        editingCommentId == null &&
                        deletingCommentId == null,
                    modifier = Modifier.semantics {
                        contentDescription = "Editar comentário"
                    }
                ) { Text("Editar") }
                TextButton(
                    onClick = { onRequestDeleteComment(comment) },
                    enabled = commentMutationLoadingId == null &&
                        editingCommentId == null &&
                        deletingCommentId == null,
                    modifier = Modifier.semantics {
                        contentDescription = "Excluir comentário"
                    }
                ) { Text("Excluir") }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (isEditing) {
            CampusHubTextField(
                value = editingCommentContent,
                onValueChange = onEditCommentContentChange,
                label = "Editar comentário",
                enabled = !isMutating,
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampusHubOutlinedButton(
                    text = "Cancelar",
                    onClick = onCancelEditComment,
                    enabled = !isMutating,
                    modifier = Modifier.weight(1f)
                )
                CampusHubButton(
                    text = "Salvar",
                    onClick = onSaveEditComment,
                    enabled = editingCommentContent.trim().isNotEmpty(),
                    isLoading = isMutating,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Text(
                text = comment.content,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun RatingSection(
    ratings: List<Rating>,
    isRatingsLoading: Boolean,
    ratingsErrorMessage: String?,
    userRating: Rating?,
    isUserRatingLoading: Boolean,
    userRatingErrorMessage: String?,
    selectedRatingScore: Int?,
    isRatingMutationLoading: Boolean,
    ratingMessage: String?,
    isRatingMessageError: Boolean,
    eventStatus: EventStatus,
    canRate: Boolean,
    onRetryRatings: () -> Unit,
    onRatingScoreSelected: (Int) -> Unit,
    onSubmitRating: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CampusHubSectionHeader(title = "Avaliações")
        Spacer(modifier = Modifier.height(12.dp))
        CampusHubCard(modifier = Modifier.fillMaxWidth()) {
            when {
                isRatingsLoading -> CampusHubLoadingState(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                ratingsErrorMessage != null -> CampusHubErrorState(
                    message = ratingsErrorMessage,
                    onRetry = onRetryRatings,
                    modifier = Modifier.fillMaxWidth()
                )
                else -> {
                    val average = calculateAverageRating(ratings)
                    if (average == null) {
                        CampusHubEmptyState(
                            message = "Ainda não há avaliações.",
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val countLabel = if (ratings.size == 1) {
                            "1 avaliação"
                        } else {
                            "${ratings.size} avaliações"
                        }
                        Text(
                            text = "Avaliação média: ${formatAverageRating(average)} / 5",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = countLabel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            userRatingErrorMessage?.let { message ->
                StatusMessage(message = message, isError = true)
            }

            if (canRate) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Avalie este evento", style = MaterialTheme.typography.titleMedium)
                if (isUserRatingLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CampusHubLoadingState(modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..5).forEach { score ->
                        if (selectedRatingScore == score) {
                            CampusHubButton(
                                text = score.toString(),
                                onClick = { onRatingScoreSelected(score) },
                                enabled = !isRatingMutationLoading && !isUserRatingLoading,
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            CampusHubOutlinedButton(
                                text = score.toString(),
                                onClick = { onRatingScoreSelected(score) },
                                enabled = !isRatingMutationLoading && !isUserRatingLoading,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                CampusHubButton(
                    text = if (userRating == null) "Enviar avaliação" else "Atualizar avaliação",
                    onClick = onSubmitRating,
                    enabled = selectedRatingScore != null && !isUserRatingLoading,
                    isLoading = isRatingMutationLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when (eventStatus) {
                        EventStatus.UPCOMING ->
                            "A avaliação estará disponível após o encerramento do evento."
                        EventStatus.ENDED ->
                            "Somente estudantes inscritos podem avaliar este evento."
                        EventStatus.UNKNOWN ->
                            "A avaliação está indisponível para este evento."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!isUserRatingLoading && userRating != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sua avaliação: ${userRating.score} / 5",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            ratingMessage?.let { message ->
                StatusMessage(message = message, isError = isRatingMessageError)
            }
        }
    }
}

@Composable
private fun StatusMessage(
    message: String,
    isError: Boolean
) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = message,
        modifier = Modifier.semantics {
            liveRegion = if (isError) LiveRegionMode.Assertive else LiveRegionMode.Polite
        },
        color = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        },
        style = MaterialTheme.typography.bodyMedium
    )
}

private fun formatAverageRating(average: Double): String =
    String.format(Locale.ROOT, "%.1f", average).replace('.', ',')

private fun formatCommentDate(timestamp: Timestamp?): String {
    return timestamp?.toDate()
        ?.toInstant()
        ?.atZone(COMMENT_TIME_ZONE)
        ?.format(COMMENT_DATE_FORMATTER)
        ?: "Enviando..."
}

@Composable
private fun EventInformation(
    label: String,
    value: String
) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge
    )
}
