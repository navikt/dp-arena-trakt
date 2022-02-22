package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.tjenester.VedtakService.Vedtak
import org.junit.jupiter.api.Test

internal class VedtakRepositoryTest {
    private val repository = VedtakRepository()

    @Test
    fun `lagrer vedtak`() {
        withMigratedDb {
            repository.lagre(
                Vedtak(
                    sakId = 1,
                    vedtakId = 2,
                    personId = 3,
                    vedtaktypekode = "O",
                    utfallkode = "JA",
                    rettighetkode = "DAGO",
                    vedtakstatuskode = "IVERK",
                )
            )
        }
    }
}
