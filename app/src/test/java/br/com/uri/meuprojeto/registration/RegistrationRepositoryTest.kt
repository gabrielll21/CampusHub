package br.com.uri.meuprojeto.registration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RegistrationRepositoryTest {

    @Test
    fun `same user and event produce the same deterministic id`() {
        assertEquals(
            "user123_event456",
            registrationDocumentId("user123", "event456")
        )
        assertEquals(
            registrationDocumentId("user123", "event456"),
            registrationDocumentId("user123", "event456")
        )
    }

    @Test
    fun `different user and event pairs produce different ids`() {
        assertNotEquals(
            registrationDocumentId("user123", "event456"),
            registrationDocumentId("user999", "event456")
        )
        assertNotEquals(
            registrationDocumentId("user123", "event456"),
            registrationDocumentId("user123", "event999")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty user id is rejected`() {
        registrationDocumentId("  ", "event456")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `slash in event id is rejected`() {
        registrationDocumentId("user123", "event/456")
    }
}
