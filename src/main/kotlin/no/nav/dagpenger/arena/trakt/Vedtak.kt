package no.nav.dagpenger.arena.trakt

internal class Vedtak(val id: String, vararg datakrav: Datakrav) {
    private val datakrav = datakrav.toList()
    fun komplett() = datakrav.all{ it.oppfyltFor(this)}
}
