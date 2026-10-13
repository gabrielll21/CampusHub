package br.com.uri.meuprojeto.rating

fun calculateAverageRating(ratings: List<Rating>): Double? {
    if (ratings.isEmpty()) {
        return null
    }

    return ratings.map { it.score }.average()
}
