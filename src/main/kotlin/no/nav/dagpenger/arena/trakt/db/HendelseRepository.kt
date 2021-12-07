package no.nav.dagpenger.arena.trakt.db

import kotliquery.Query
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.datakrav.Datakrav
import no.nav.dagpenger.arena.trakt.serde.HendelseVisitor
import no.nav.dagpenger.arena.trakt.serde.VedtakHendelseJsonBuilder
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

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

    fun leggPåKø(hendelse: Hendelse): Boolean {
        if (ferdigeHendelser.contains(hendelse)) return true

        hendelser.add(hendelse)

        return finnOgPubliserFerdigeHendelser().isNotEmpty()
    }

    internal fun finnOgPubliserFerdigeHendelser() = hendelser.filter { it.alleDatakravOppfylt() }
        .onEach { ferdigHendelse ->
            when (ferdigHendelse.hendelseId.objekt) {
                Hendelse.Type.BeregningUtført -> TODO()
                Hendelse.Type.Vedtak -> VedtakHendelseJsonBuilder(ferdigHendelse).resultat()
            }.also {
                rapidsConnection.publish(it.toString())
                sikkerlogg.info { "Publiserte ferdig hendelse: ${it.toPrettyString()}" }
            }
        }.onEach {
            ferdigeHendelser.add(it)
            hendelser.remove(it)
            markerDataSomBrukt(it)
        }

    private fun markerDataSomBrukt(hendelse: Hendelse) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            MarkerSomBruktVisitor(hendelse).queries().forEach { query -> session.run(query.asUpdate) }
        }
}

private class MarkerSomBruktVisitor(hendelse: Hendelse) : HendelseVisitor {
    private lateinit var hendelseId: UUID
    private val queries = mutableListOf<Query>()

    init {
        require(hendelse.alleDatakravOppfylt())
        hendelse.accept(this)
    }

    fun queries() = queries

    override fun preVisit(hendelse: Hendelse, type: Hendelse.Type, id: String) {
        hendelseId = hendelse.hendelseId.uuid
    }

    override fun <T> visit(datakrav: Datakrav<T>, id: String, resultat: Datakrav.Resultat<T>?, oppfylt: Boolean) {
        resultat?.let {
            //language=PostgreSQL
            queries.add(queryOf("UPDATE arena_data SET hendelse_id=? WHERE id=?", hendelseId, resultat.id))
        }
    }
}
