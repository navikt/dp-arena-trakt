package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class ReplikeringsloggTest {
    private val repository = Replikeringslogg()

    @Test
    @Disabled
    fun `Lagrer mottak av en rad`() {
        withMigratedDb {
            // assertFalse(repository.opprettEllerFinn("a", "I", LocalDateTime.now(), "01").settFør())
            // assertTrue(repository.opprettEllerFinn("a", "I", LocalDateTime.now(), "01").settFør())
            // assertFalse(repository.opprettEllerFinn("a", "I", LocalDateTime.now(), "02").settFør())
        }
    }
}
