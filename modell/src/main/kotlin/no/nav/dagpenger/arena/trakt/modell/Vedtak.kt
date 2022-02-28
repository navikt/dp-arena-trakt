package no.nav.dagpenger.arena.trakt.modell

data class Vedtak(
    val sakId: Int,
    val vedtakId: Int,
    val personId: Int,
    val vedtaktypekode: String,
    val utfallkode: String,
    val rettighetkode: String,
    val vedtakstatuskode: String,
)
