package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.datakrav.Datakrav

internal class Hendelse(val type: Type, val id: String, vararg datakrav: Datakrav) {
    private val datakrav = datakrav.toList()
    fun komplett() = datakrav.all { it.oppfyltFor(this) }

    enum class Type {
        VedtakIverksatt,
        VedtakEndret,
        BeregningUtført
    }
}
