package no.nav.dagpenger.arena.trakt.db

import kotlinx.coroutines.delay
import kotliquery.Query
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.datakrav.Datakrav
import no.nav.dagpenger.arena.trakt.db.DataRepository.DataObserver
import no.nav.dagpenger.arena.trakt.serde.HendelseVisitor
import no.nav.dagpenger.arena.trakt.serde.VedtakHendelseJsonBuilder
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.hendelse")

internal class HendelseRepository private constructor(
    private val hendelser: MutableSet<Hendelse>,
    private val ferdigeHendelser: MutableSet<Hendelse>,
    private val rapidsConnection: RapidsConnection
) : DataObserver {
    private var harNyData: Boolean = false

    constructor(rapidsConnection: RapidsConnection) : this(
        hendelser = mutableSetOf(),
        ferdigeHendelser = mutableSetOf(),
        rapidsConnection = rapidsConnection
    )

    suspend fun start(pollMs: Long? = 1000) {
        do {
            if (harNyData) {
                logg.info { "Poller etter nye hendelser" }
                finnOgPubliserFerdigeHendelser().also {
                    logg.info { "Ferdig å publisere ferdige hendelser. Fant ${it.size} hendelser som var ferdige." }
                    harNyData = false
                }
            } else logg.info { "Har ikke ny data, sjekker ikke etter ferdige hendelser" }

            if (pollMs !== null) delay(pollMs)
        } while (pollMs !== null)
    }

    fun leggPåKø(hendelse: Hendelse): Boolean {
        if (ferdigeHendelser.contains(hendelse)) return true

        hendelser.add(hendelse)

        return finnOgPubliserFerdigeHendelser().isNotEmpty()
    }

    private fun finnOgPubliserFerdigeHendelser() = hendelser.filter { it.alleDatakravOppfylt() }
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

    override fun nyData() {
        harNyData = true
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
