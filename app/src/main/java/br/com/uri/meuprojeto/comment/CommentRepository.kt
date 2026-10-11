package br.com.uri.meuprojeto.comment

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CommentRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val firestore = firestore

    fun addComment(
        eventId: String,
        authorId: String,
        authorName: String,
        content: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val normalizedEventId = requireValue(eventId, "eventId", onError) ?: return
        val normalizedAuthorId = requireValue(authorId, "authorId", onError) ?: return
        val normalizedAuthorName = requireValue(authorName, "authorName", onError) ?: return
        val normalizedContent = requireValue(content, "content", onError) ?: return

        val commentData = mapOf(
            "eventId" to normalizedEventId,
            "authorId" to normalizedAuthorId,
            "authorName" to normalizedAuthorName,
            "content" to normalizedContent,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to null
        )

        commentsCollection(normalizedEventId)
            .add(commentData)
            .addOnSuccessListener { document -> onSuccess(document.id) }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao adicionar comentário", exception)
                onError(exception)
            }
    }

    fun getComments(
        eventId: String,
        onSuccess: (List<Comment>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val normalizedEventId = requireValue(eventId, "eventId", onError) ?: return

        commentsCollection(normalizedEventId)
            .get()
            .addOnSuccessListener { snapshot ->
                val comments = snapshot.documents
                    .mapNotNull { document ->
                        val comment = document.toComment(normalizedEventId)
                        if (comment == null) {
                            Log.w(
                                TAG,
                                "Comentário ${document.id} ignorado por possuir campos inválidos"
                            )
                        }
                        comment
                    }
                    .sortedBy { comment ->
                        comment.createdAt?.toDate()?.time ?: Long.MAX_VALUE
                    }

                onSuccess(comments)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao consultar comentários", exception)
                onError(exception)
            }
    }

    fun updateComment(
        eventId: String,
        commentId: String,
        authorId: String,
        content: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val normalizedEventId = requireValue(eventId, "eventId", onError) ?: return
        val normalizedCommentId = requireValue(commentId, "commentId", onError) ?: return
        val normalizedAuthorId = requireValue(authorId, "authorId", onError) ?: return
        val normalizedContent = requireValue(content, "content", onError) ?: return
        val commentReference = commentsCollection(normalizedEventId)
            .document(normalizedCommentId)

        commentReference
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onError(IllegalStateException("Comentário não encontrado"))
                } else if (document.getString("authorId") != normalizedAuthorId) {
                    onError(IllegalStateException("Usuário não é o autor do comentário"))
                } else {
                    commentReference
                        .update(
                            mapOf(
                                "content" to normalizedContent,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Erro ao atualizar comentário", exception)
                            onError(exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao consultar comentário para atualização", exception)
                onError(exception)
            }
    }

    fun deleteComment(
        eventId: String,
        commentId: String,
        authorId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val normalizedEventId = requireValue(eventId, "eventId", onError) ?: return
        val normalizedCommentId = requireValue(commentId, "commentId", onError) ?: return
        val normalizedAuthorId = requireValue(authorId, "authorId", onError) ?: return
        val commentReference = commentsCollection(normalizedEventId)
            .document(normalizedCommentId)

        commentReference
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onError(IllegalStateException("Comentário não encontrado"))
                } else if (document.getString("authorId") != normalizedAuthorId) {
                    onError(IllegalStateException("Usuário não é o autor do comentário"))
                } else {
                    commentReference
                        .delete()
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Erro ao excluir comentário", exception)
                            onError(exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao consultar comentário para exclusão", exception)
                onError(exception)
            }
    }

    private fun commentsCollection(eventId: String) =
        firestore
            .collection(EVENTS_COLLECTION)
            .document(eventId)
            .collection(COMMENTS_COLLECTION)

    private fun requireValue(
        value: String,
        fieldName: String,
        onError: (Exception) -> Unit
    ): String? {
        val normalizedValue = value.trim()
        if (normalizedValue.isNotEmpty()) {
            return normalizedValue
        }

        onError(IllegalArgumentException("$fieldName não pode ser vazio"))
        return null
    }

    private fun DocumentSnapshot.toComment(eventId: String): Comment? {
        val storedEventId = getString("eventId")
        val authorId = getString("authorId")
        val authorName = getString("authorName")
        val content = getString("content")

        if (
            storedEventId.isNullOrBlank() ||
            storedEventId != eventId ||
            authorId.isNullOrBlank() ||
            authorName.isNullOrBlank() ||
            content.isNullOrBlank()
        ) {
            return null
        }

        return Comment(
            id = id,
            eventId = storedEventId,
            authorId = authorId,
            authorName = authorName,
            content = content,
            createdAt = getOptionalTimestamp("createdAt"),
            updatedAt = getOptionalTimestamp("updatedAt")
        )
    }

    private fun DocumentSnapshot.getOptionalTimestamp(field: String): Timestamp? =
        try {
            getTimestamp(field)
        } catch (exception: RuntimeException) {
            Log.w(
                TAG,
                "Campo $field do comentário $id possui um tipo inválido; usando null",
                exception
            )
            null
        }

    private companion object {
        const val TAG = "CommentRepository"
        const val EVENTS_COLLECTION = "events"
        const val COMMENTS_COLLECTION = "comments"
    }
}
