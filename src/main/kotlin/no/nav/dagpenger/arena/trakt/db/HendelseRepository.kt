package no.nav.dagpenger.arena.trakt.db

import no.nav.dagpenger.arena.trakt.Hendelse

internal class HendelseRepository(
    // TODO: Bør lagres i Database for å tåle restarts
    private val hendelser: MutableList<Hendelse> = mutableListOf()
) {
    fun leggTilVent(hendelse: Hendelse) = hendelser.add(hendelse)

    fun finnFerdigeHendelser() = hendelser.filter { it.komplett() }.onEach(::fjern)

    private fun fjern(hendelse: Hendelse) = hendelser.remove(hendelse)
}
