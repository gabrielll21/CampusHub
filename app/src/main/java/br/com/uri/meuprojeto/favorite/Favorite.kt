package br.com.uri.meuprojeto.favorite

import com.google.firebase.Timestamp

data class Favorite(
    val eventId: String,
    val userId: String,
    val createdAt: Timestamp?
)
