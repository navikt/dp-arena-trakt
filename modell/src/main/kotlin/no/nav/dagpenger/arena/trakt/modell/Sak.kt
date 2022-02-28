package no.nav.dagpenger.arena.trakt.modell

class Sak(
    val sakId: Int,
    private val erDagpenger: Boolean?,
    private val vedtakListe: MutableSet<Vedtak>,
) {
    internal constructor(sakId: Int) : this(sakId, null, mutableSetOf())
    constructor(sakId: Int, erDagpenger: Boolean) : this(sakId, erDagpenger, mutableSetOf())

    fun håndter(sak: Sak) = if (sakId == sak.sakId) {
        this + sak
        true
    } else {
        false
    }

    private fun skalBehandle(sak: Sak, block: (sak: Sak) -> Any): Any? {
        return if (sakId == sak.sakId)
            block(sak) else null
    }

    fun håndter(vedtak: Vedtak): Set<Vedtak> = vedtakListe + setOf(vedtak)

    operator fun plus(nySak: Sak) = this.apply { vedtakListe + nySak.vedtakListe }
}
