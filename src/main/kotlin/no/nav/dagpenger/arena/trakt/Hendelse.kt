package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.datakrav.Beregningsledd
import no.nav.dagpenger.arena.trakt.datakrav.Datakrav
import no.nav.dagpenger.arena.trakt.datakrav.Vedtak
import no.nav.dagpenger.arena.trakt.datakrav.Vedtaksfakta

internal class Hendelse(
    val type: Type,
    val id: String,
    val datakrav: KravBygger.() -> Unit
) {
    private val krav = mutableListOf<Datakrav<*>>()

    init {
        KravBygger(this).also {
            datakrav(it)
        }
    }

    fun komplett() = krav.all { it.oppfylt() }

    enum class Type {
        VedtakIverksatt,
        VedtakEndret,
        BeregningUtført
    }

    internal class KravBygger(val hendelse: Hendelse) {
        inline fun <reified T : Datakrav<*>> krev(id: String) {
            hendelse.krav.add(T::class.java.getConstructor().newInstance(id, hendelse))
        }
    }
}

private val b = Hendelse(Hendelse.Type.BeregningUtført, "123") {
    krev<Beregningsledd>("123")
    krev<Vedtaksfakta>("123")
    krev<Vedtak>("123")
}.komplett()
