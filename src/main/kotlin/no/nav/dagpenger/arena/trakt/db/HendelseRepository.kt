package no.nav.dagpenger.arena.trakt.db

import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.serde.VedtakHendelseJsonBuilder
import no.nav.helse.rapids_rivers.RapidsConnection

private val sikkerlogg = KotlinLogging.logger("tjenestekall.hendelse")

internal class HendelseRepository private constructor(
    private val hendelser: MutableSet<Hendelse>,
    private val ferdigeHendelser: MutableSet<Hendelse>,
    private val rapidsConnection: RapidsConnection
) {
    constructor(rapidsConnection: RapidsConnection) : this(
        hendelser = mutableSetOf(),
        ferdigeHendelser = mutableSetOf(),
        rapidsConnection = rapidsConnection
    )

    fun leggPåKø(hendelse: Hendelse): List<Hendelse> {
        if (ferdigeHendelser.contains(hendelse)) return emptyList()

        hendelser.add(hendelse)

        return finnFerdigeHendelser()
    }

    fun finnFerdigeHendelser(): List<Hendelse> = hendelser.filter { it.komplett() }
        .onEach { ferdigHendelse ->
            when (ferdigHendelse.type) {
                Hendelse.Type.BeregningUtført -> TODO()
                Hendelse.Type.Vedtak -> VedtakHendelseJsonBuilder(ferdigHendelse).resultat()
            }.also {
                rapidsConnection.publish(it.toString())
                sikkerlogg.info { "Publiserte ferdig hendelse: ${it.toPrettyString()}" }
            }
        }.onEach {
            ferdigeHendelser.add(it)
            hendelser.remove(it)
        }
}
