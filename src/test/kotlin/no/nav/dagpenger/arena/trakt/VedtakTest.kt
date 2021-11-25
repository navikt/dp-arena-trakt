package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.BeregningsleddRepository
import no.nav.dagpenger.arena.trakt.db.VedtaksfaktaRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class VedtakTest {
    private val vedtaksid = "12341785"
    private val beregningsleddRepository = BeregningsleddRepository()
    private val vedtaksfaktaRepository = VedtaksfaktaRepository()

    @Test
    fun `lager vedtak`() {
        val vedtak = Vedtak(vedtaksid,
            Beregningsledd("DPTEL", beregningsleddRepository),
            Vedtaksfakta("ANTB", vedtaksfaktaRepository))
        assertFalse(vedtak.komplett())

    }

    @Test
    fun `Vedtak skal være komplett`() {
        val vedtak = Vedtak(
            vedtaksid,
            Beregningsledd("DPTEL", beregningsleddRepository),
            Vedtaksfakta("ANTB",vedtaksfaktaRepository)
        )

    }
}
