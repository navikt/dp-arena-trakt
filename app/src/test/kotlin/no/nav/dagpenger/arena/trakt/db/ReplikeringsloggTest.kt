package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.meldinger.SakReplikertMelding
import no.nav.helse.rapids_rivers.JsonMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ReplikeringsloggTest {
    private val repository = Replikeringslogg()
    private val message = SakReplikertMelding(
        JsonMessage.newMessage(
            mapOf(
                "table" to "SIAMO.SAK",
                "pos" to "0001",
                "op_type" to "I",
                "op_ts" to "2021-11-18 11:37:16.455389"
            )
        ).apply {
            requireKey("table", "pos", "op_type", "op_ts")
            interestedIn("after.SAK_ID", "after.SAKSKODE")
        }
    )

    @Test
    fun `Lagrer mottak av en rad, og kan sjekke om den er behandlet`() {
        withMigratedDb {
            repository.lagre(message)
            assertFalse(repository.erBehandlet(message.id))
            assertEquals(1, repository.markerSomBehandlet(message.id))
            assertTrue(repository.erBehandlet(message.id))
        }
    }
}
