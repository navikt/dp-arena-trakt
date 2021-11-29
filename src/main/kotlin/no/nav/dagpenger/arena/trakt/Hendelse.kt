package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.datakrav.Datakrav
import no.nav.dagpenger.arena.trakt.serde.HendelseVisitor

internal class Hendelse(
    private val type: Type,
    internal val id: String,
    internal val kravbygger: KravBygger.() -> Unit
) {
    private val datakrav = mutableListOf<Datakrav<*>>()

    init {
        KravBygger(this).also {
            kravbygger(it)
        }
    }

    fun komplett() = datakrav.all { it.oppfylt() }

    internal fun accept(visitor: HendelseVisitor) {
        visitor.preVisit(this, type, id)
        datakrav.forEach { it.accept(visitor) }
        visitor.postVisit(this, type, id)
    }

    enum class Type {
        VedtakIverksatt,
        VedtakEndret,
        BeregningUtført
    }

    internal class KravBygger(val hendelse: Hendelse) {
        fun <T> krev(datakrav: Datakrav<T>) {
            datakrav.hendelse = hendelse
            hendelse.datakrav.add(datakrav)
        }
    }
}
