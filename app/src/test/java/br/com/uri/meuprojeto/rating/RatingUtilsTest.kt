package br.com.uri.meuprojeto.rating

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RatingUtilsTest {

    @Test
    fun `empty ratings have no average`() {
        assertNull(calculateAverageRating(emptyList()))
    }

    @Test
    fun `one rating keeps its score`() {
        assertEquals(4.0, calculateAverageRating(listOf(rating(4))))
    }

    @Test
    fun `multiple ratings calculate decimal average`() {
        assertEquals(4.333333333333333, calculateAverageRating(listOf(rating(5), rating(4), rating(4))))
    }

    private fun rating(score: Int) = Rating(
        userId = "user-id",
        eventId = "event-id",
        score = score
    )
}
