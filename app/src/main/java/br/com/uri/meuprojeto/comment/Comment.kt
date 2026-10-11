package br.com.uri.meuprojeto.comment

import com.google.firebase.Timestamp

data class Comment(
    val id: String,
    val eventId: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
