package no.nav.dagpenger.arena.trakt

import java.time.LocalDateTime

data class Vedtak(
    val sakId: Int,
    val vedtakId: Int,
    val personId: Int,
    val vedtaktypekode: String,
    val utfallkode: String,
    val rettighetkode: String,
    val vedtakstatuskode: String,
    val opprettet: LocalDateTime,
    val oppdatert: LocalDateTime,
    val saknummer: String,
)
