package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.sakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ArenaRadTest {

    @Test
    fun `Json data blir opprettet som riktig type av ArenaRad`() {
        val sakRad = ArenaRad.lagRad("SIAMO.SAK", sakJSON())
        assertTrue(sakRad is SakRad)

        val vedtakRad = ArenaRad.lagRad("SIAMO.VEDTAK", vedtakJSON())
        assertTrue(vedtakRad is VedtakRad)

        val vedtakFaktaRad = ArenaRad.lagRad("SIAMO.VEDTAKFAKTA", vedtaksfaktaJSON())
        assertTrue(vedtakFaktaRad is VedtakFaktaRad)

        val beregningsleddRad = ArenaRad.lagRad("SIAMO.BEREGNINGSLEDD", beregningsleddJSON())
        assertTrue(beregningsleddRad is BeregningsleddRad)
    }

    @Test
    fun `Ukjent tabelltype`() {
        assertThrows<IllegalArgumentException> { ArenaRad.lagRad("UKJENT", sakJSON()) }
    }
}