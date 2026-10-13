package br.com.uri.meuprojeto.rating

import org.junit.Test

class RatingRepositoryTest {

    @Test
    fun `scores from one to five are valid`() {
        (1..5).forEach(::validateRatingScore)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero is rejected`() {
        validateRatingScore(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative score is rejected`() {
        validateRatingScore(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `score above five is rejected`() {
        validateRatingScore(6)
    }
}
