package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.db.ArenaKoder.BEREGNINGSLEDD_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.SAK_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.VEDTAKFAKTA_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.VEDTAK_TABELL
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.sakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ArenaRadTest {

    @Test
    fun `Json data blir opprettet som riktig type av ArenaRad`() {
        val sakRad = ArenaRad.lagRad(SAK_TABELL, sakJSON())
        assertTrue(sakRad is SakRad)

        val vedtakRad = ArenaRad.lagRad(VEDTAK_TABELL, vedtakJSON())
        assertTrue(vedtakRad is VedtakRad)

        val vedtakFaktaRad = ArenaRad.lagRad(VEDTAKFAKTA_TABELL, vedtaksfaktaJSON())
        assertTrue(vedtakFaktaRad is VedtakFaktaRad)

        val beregningsleddRad = ArenaRad.lagRad(BEREGNINGSLEDD_TABELL, beregningsleddJSON())
        assertTrue(beregningsleddRad is BeregningsleddRad)
    }

    @Test
    fun `Ukjent tabelltype`() {
        assertThrows<IllegalArgumentException> { ArenaRad.lagRad("UKJENT", sakJSON()) }
    }
}
