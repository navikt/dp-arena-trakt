package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.tjenester.VedtakService.Vedtak
import org.junit.jupiter.api.Test

internal class VedtakRepositoryTest {
    private val repository = VedtakRepository()

    @Test
    fun `lagrer vedtak`() {
        withMigratedDb {
            repository.lagreVedtak(
                Vedtak(
                    sakId = 1,
                    vedtakId = 2,
                    personId = 3,
                    vedtaktypekode = "IVERK",
                    utfallkode = "Ja",
                    rettighetkode = "ORD",
                    vedtakstatuskode = "",
                )
            )
        }
    }
}
