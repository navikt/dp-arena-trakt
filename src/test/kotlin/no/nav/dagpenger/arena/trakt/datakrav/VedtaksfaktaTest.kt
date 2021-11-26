package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.IverksattVedtak
import no.nav.dagpenger.arena.trakt.VedtaksFaktaJSON
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtaksfaktaTest {
    private val repository = DataRepository()
    private val vedtaksfakta = Vedtaksfakta("ENDRTILUNN")
    private val vedtak = IverksattVedtak("123", vedtaksfakta)

    @Test
    fun `Vedtaksfaktakrav er ikke oppfylt`() {
        withMigratedDb {
            assertFalse(vedtaksfakta.oppfyltFor(vedtak))
        }
    }

    @Test
    fun `Vedtaksfaktakrav er oppfylt`() {
        withMigratedDb {
            repository.lagre(VedtaksFaktaJSON)
            assertTrue(vedtaksfakta.oppfyltFor(vedtak))
        }
    }
}
