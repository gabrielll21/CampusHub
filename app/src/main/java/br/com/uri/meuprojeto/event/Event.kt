package br.com.uri.meuprojeto.event

import com.google.firebase.Timestamp

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val startsAt: Timestamp? = null,
    val category: String? = null
)
