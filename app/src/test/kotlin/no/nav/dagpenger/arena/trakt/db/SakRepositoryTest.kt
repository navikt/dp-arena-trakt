package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.tjenester.SakSink
import org.junit.jupiter.api.Test

internal class SakRepositoryTest {
    private val repository = SakRepository()

    @Test
    fun `lagrer sak`() {
        withMigratedDb {
            repository.lagre(
                SakSink.Sak(
                    sakId = 1,
                    erDagpenger = true
                )
            )
        }
    }
}
