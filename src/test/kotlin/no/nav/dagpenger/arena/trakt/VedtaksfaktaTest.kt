package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.VedtaksfaktaRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtaksfaktaTest {
    private val repository = VedtaksfaktaRepository()
    private val vedtaksfakta = Vedtaksfakta("ENDRTILUNN", repository)
    private val vedtak = Vedtak("123", vedtaksfakta)

    @Test
    fun `Vedtaksfaktakrav er ikke oppfylt`() {
        withMigratedDb {
            assertFalse(vedtaksfakta .oppfyltFor(vedtak))
        }
    }

    @Test
    fun `Vedtaksfaktakrav er oppfylt`() {
        withMigratedDb {
            VedtaksfaktaRepository().insert(VedtaksFaktaJSON)
            assertTrue(vedtaksfakta.oppfyltFor(vedtak))
        }
    }
}
