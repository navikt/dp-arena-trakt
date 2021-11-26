package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.IverksattVedtak
import no.nav.dagpenger.arena.trakt.VedtakJSON
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtakTest {
    private val repository = DataRepository()
    private val vedtak = Vedtak()
    private val iverksattVedtak = IverksattVedtak("123", vedtak)

    @Test
    fun `Vedtak finnes ikke enda`() {
        withMigratedDb {
            assertFalse(vedtak.oppfyltFor(iverksattVedtak))
        }
    }

    @Test
    fun `Vedtak finnes`() {
        withMigratedDb {
            repository.lagre(VedtakJSON)
            assertTrue(vedtak.oppfyltFor(iverksattVedtak))
        }
    }
}
