package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.datakrav.Beregningsledd
import no.nav.dagpenger.arena.trakt.datakrav.Vedtaksfakta
import no.nav.dagpenger.arena.trakt.db.BeregningsleddRepository
import no.nav.dagpenger.arena.trakt.db.VedtaksfaktaRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class IverksattVedtakTest {
    private val vedtaksid = "123"
    private val beregningsleddRepository = BeregningsleddRepository()
    private val vedtaksfaktaRepository = VedtaksfaktaRepository()

    @Test
    fun `Vedtak skal ikke være komplett`() {
        Postgres.withMigratedDb {
            val vedtak = IverksattVedtak(
                vedtaksid,
                Beregningsledd("DPTEL", beregningsleddRepository),
                Vedtaksfakta("ANTB", vedtaksfaktaRepository)
            )
            assertFalse(vedtak.komplett())
        }
    }

    @Test
    fun `Vedtak skal være komplett`() {
        Postgres.withMigratedDb {
            val vedtak = IverksattVedtak(
                vedtaksid,
                Beregningsledd("DPTEL", beregningsleddRepository),
                Vedtaksfakta("ENDRTILUNN", vedtaksfaktaRepository)
            )

            BeregningsleddRepository().insert(BeregningsleddJSON)
            VedtaksfaktaRepository().insert(VedtaksFaktaJSON)
            assertTrue(vedtak.komplett())
        }
    }
}
