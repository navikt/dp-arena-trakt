package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.IverksattVedtak

internal abstract class Datakrav() {
    internal abstract fun oppfyltFor(vedtak: IverksattVedtak): Boolean
}

