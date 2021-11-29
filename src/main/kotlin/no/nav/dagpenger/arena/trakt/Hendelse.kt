package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.datakrav.Beregningsledd
import no.nav.dagpenger.arena.trakt.datakrav.Datakrav
import no.nav.dagpenger.arena.trakt.datakrav.Vedtak
import no.nav.dagpenger.arena.trakt.datakrav.Vedtaksfakta
import no.nav.dagpenger.arena.trakt.serde.HendelseVisitor

// TODO: Private constructor
internal class Hendelse(
    val type: Type,
    internal val id: String,
    internal val kravbygger: KravBygger.() -> Unit
) {
    private val datakrav = mutableListOf<Datakrav<*>>()

    companion object {
        fun vedtakIverksatt(id: String) = Hendelse(Type.VedtakIverksatt, id) {
            krev(Beregningsledd("BL1"))
            krev(Vedtaksfakta("VF1"))
            krev(Vedtak(id))
        }

        fun vedtakEndret(id: String) = Hendelse(Type.VedtakEndret, id) {
            krev(Beregningsledd("BL1"))
            krev(Vedtaksfakta("VF1"))
            krev(Vedtak(id))
        }
    }

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

    internal enum class Type {
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
