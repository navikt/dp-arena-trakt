package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.IverksattVedtak
import no.nav.dagpenger.arena.trakt.db.BeregningsleddRepository

internal class Beregningsledd(private val navn: String, private val beregningsleddRepository: BeregningsleddRepository) : Datakrav() {
    override fun oppfyltFor(vedtak: IverksattVedtak): Boolean {
        // realtertObjektType + vedtakid er kobling til vedtak
        return beregningsleddRepository.finn(navn, "VEDTAK", vedtak.id)
    }
}