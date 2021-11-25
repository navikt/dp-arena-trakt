package no.nav.dagpenger.arena.trakt

internal class IverksattVedtak(val id: String, vararg datakrav: Datakrav) {
    private val datakrav = datakrav.toList()
    fun komplett() = datakrav.all { it.oppfyltFor(this) }
}
