package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.datakrav.Datakrav
import no.nav.dagpenger.arena.trakt.serde.HendelseVisitor
import java.util.UUID

internal class Hendelse internal constructor(
    internal val hendelseId: HendelseId,
    internal val kravbygger: KravBygger.() -> Unit,
) {
    private val datakrav = mutableListOf<Datakrav<*>>()

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
