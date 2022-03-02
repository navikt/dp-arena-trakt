package no.nav.dagpenger.arena.trakt

import java.time.LocalDateTime

data class Sak(
    val sakId: Int,
    val erDagpenger: Boolean,
    val opprettet: LocalDateTime,
    val oppdatert: LocalDateTime,
)
