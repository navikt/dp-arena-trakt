package no.nav.dagpenger.arena.trakt

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class DatakravTest {
    val beregningsledd = Beregningsledd("DPTEL")
    val vedtak = Vedtak("872397432", beregningsledd)

    @Test
    fun `Er ikke oppfylt`() {
        assertFalse(beregningsledd.oppfyltFor(vedtak))
    }
}
