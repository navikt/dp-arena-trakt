package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.Sak
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import org.junit.jupiter.api.Test

internal class SakRepositoryTest {
    private val repository = SakRepository()

    @Test
    fun `lagrer sak`() {
        withMigratedDb {
            repository.lagre(Sak(sakId = 1, erDagpenger = true))
        }
    }
}
