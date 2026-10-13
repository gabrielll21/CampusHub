package br.com.uri.meuprojeto.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.uri.meuprojeto.comment.Comment
import br.com.uri.meuprojeto.event.Event
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val COMMENT_TIME_ZONE = ZoneId.of("America/Sao_Paulo")
private val COMMENT_DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm", Locale.ROOT)

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
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
        ) {
            Text(
                text = "CampusHub",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = event.title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineMedium
            )

            if (isFavoriteLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .size(28.dp),
                    strokeWidth = 2.dp
                )
            } else if (isFavorite != null && event.id.isNotBlank()) {
                IconButton(
                    onClick = onToggleFavorite,
                    enabled = !isFavoriteLoading,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .semantics {
                            contentDescription = if (isFavorite) {
                                "Remover dos favoritos"
                            } else {
                                "Adicionar aos favoritos"
                            }
                        }
                ) {
                    Text(
                        text = if (isFavorite) "♥" else "♡",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            favoriteMessage?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.semantics {
                        liveRegion = if (isFavoriteMessageError) {
                            LiveRegionMode.Assertive
                        } else {
                            LiveRegionMode.Polite
                        }
                    },
                    color = if (isFavoriteMessageError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    EventInformation(label = "Data", value = event.date)
                    Spacer(modifier = Modifier.height(20.dp))
                    EventInformation(
                        label = "Localização",
                        value = event.location
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Sobre o evento",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = event.description,
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Inscrições",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isRegistrationLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.CenterHorizontally),
                            strokeWidth = 2.dp
                        )
                    } else if (isRegistered) {
                        OutlinedButton(
                            onClick = onCancelRegistration,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar inscrição")
                        }
                    } else {
                        Button(
                            onClick = onRegister,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Inscrever-se")
                        }
                    }

                    registrationMessage?.let { message ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = message,
                            modifier = Modifier.semantics {
                                liveRegion = if (isRegistrationMessageError) {
                                    LiveRegionMode.Assertive
                                } else {
                                    LiveRegionMode.Polite
                                }
                            },
                            color = if (isRegistrationMessageError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Comentários",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isCommentsLoading -> CircularProgressIndicator(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.CenterHorizontally),
                    strokeWidth = 2.dp
                )

                commentsErrorMessage != null -> Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = commentsErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = onRetryComments) {
                            Text("Tentar novamente")
                        }
                    }
                }

                comments.isEmpty() -> Text(
                    text = "Seja o primeiro a comentar.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )

                else -> Column(modifier = Modifier.fillMaxWidth()) {
                    comments.forEach { comment ->
                        val isOwnComment = comment.authorId == currentUserId
                        val isEditing = comment.id == editingCommentId
                        val isMutating = comment.id == commentMutationLoadingId

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                                        Text(
                                            text = comment.authorName,
                                            style = MaterialTheme.typography.titleMedium
                                        )
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
                                    }

                                    if (isOwnComment && !isEditing) {
                                        TextButton(
                                            onClick = { onStartEditComment(comment) },
                                            enabled = commentMutationLoadingId == null &&
                                                editingCommentId == null &&
                                                deletingCommentId == null,
                                            modifier = Modifier.semantics {
                                                contentDescription = "Editar comentário"
                                            }
                                        ) {
                                            Text("Editar")
                                        }
                                        TextButton(
                                            onClick = { onRequestDeleteComment(comment) },
                                            enabled = commentMutationLoadingId == null &&
                                                editingCommentId == null &&
                                                deletingCommentId == null,
                                            modifier = Modifier.semantics {
                                                contentDescription = "Excluir comentário"
                                            }
                                        ) {
                                            Text("Excluir")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (isEditing) {
                                    OutlinedTextField(
                                        value = editingCommentContent,
                                        onValueChange = onEditCommentContentChange,
                                        enabled = !isMutating,
                                        label = { Text("Editar comentário") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        maxLines = 5
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(
                                            onClick = onCancelEditComment,
                                            enabled = !isMutating,
                                            modifier = Modifier.fillMaxWidth(0.5f)
                                        ) {
                                            Text("Cancelar")
                                        }
                                        Button(
                                            onClick = onSaveEditComment,
                                            enabled = !isMutating &&
                                                editingCommentContent.trim().isNotEmpty(),
                                            modifier = Modifier.fillMaxWidth(0.5f)
                                        ) {
                                            if (isMutating) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text("Salvar")
                                            }
                                        }
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = commentContent,
                onValueChange = onCommentContentChange,
                enabled = !isCommentSubmitting,
                label = { Text("Novo comentário") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSubmitComment,
                enabled = !isCommentSubmitting && commentContent.trim().isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCommentSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Comentar")
                }
            }

            commentSubmissionMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    modifier = Modifier.semantics {
                        liveRegion = if (isCommentSubmissionError) {
                            LiveRegionMode.Assertive
                        } else {
                            LiveRegionMode.Polite
                        }
                    },
                    color = if (isCommentSubmissionError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            commentMutationMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    modifier = Modifier.semantics {
                        liveRegion = if (isCommentMutationMessageError) {
                            LiveRegionMode.Assertive
                        } else {
                            LiveRegionMode.Polite
                        }
                    },
                    color = if (isCommentMutationMessageError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voltar")
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
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
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
