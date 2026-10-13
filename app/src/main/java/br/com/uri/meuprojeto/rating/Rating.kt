package br.com.uri.meuprojeto.rating

import com.google.firebase.Timestamp

data class Rating(
    val userId: String,
    val eventId: String,
    val score: Int,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
