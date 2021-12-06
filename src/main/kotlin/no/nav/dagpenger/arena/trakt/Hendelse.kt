package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.datakrav.Beregningsledd
import no.nav.dagpenger.arena.trakt.datakrav.Datakrav
import no.nav.dagpenger.arena.trakt.datakrav.Vedtak
import no.nav.dagpenger.arena.trakt.datakrav.Vedtaksfakta
import no.nav.dagpenger.arena.trakt.serde.HendelseVisitor
import java.util.UUID

internal class Hendelse private constructor(
    internal val hendelseId: HendelseId,
    internal val kravbygger: KravBygger.() -> Unit,
) {
    private val datakrav = mutableListOf<Datakrav<*>>()

    companion object {
        fun testHendelse(id: String) = Hendelse(HendelseId(Type.Vedtak, id)) {
            krev(Beregningsledd("BL1"))
            krev(Vedtaksfakta("VF1"))
            krev(Vedtak(id))
        }

        fun vedtak(id: String) = Hendelse(HendelseId(Type.Vedtak, id)) {
            krev(Beregningsledd("DPTEL"))
            krev(Vedtaksfakta("FDATO"))
            krev(Vedtak(id))
        }
    }

    init {
        KravBygger(this).also {
            kravbygger(it)
        }
    }

    fun alleDatakravOppfylt() = datakrav.all { it.oppfylt() }

    internal fun accept(visitor: HendelseVisitor) {
        visitor.preVisit(this, hendelseId.objekt, hendelseId.objektId)
        datakrav.forEach { it.accept(visitor) }
        visitor.postVisit(this, hendelseId.objekt, hendelseId.objektId)
    }

    internal enum class Type {
        BeregningUtført,
        Vedtak
    }

    internal data class HendelseId(val objekt: Type, val objektId: String, val uuid: UUID = UUID.randomUUID()) {
        override fun equals(other: Any?) = other is HendelseId && objekt == other.objekt && objektId == other.objektId
        override fun hashCode() = Pair(objekt, objektId).hashCode()
    }

    internal class KravBygger(val hendelse: Hendelse) {
        fun <T> krev(datakrav: Datakrav<T>) {
            datakrav.hendelse = hendelse.hendelseId
            hendelse.datakrav.add(datakrav)
        }
    }

    override fun equals(other: Any?) = other is Hendelse && hendelseId == other.hendelseId
    override fun hashCode() = hendelseId.hashCode()
}
