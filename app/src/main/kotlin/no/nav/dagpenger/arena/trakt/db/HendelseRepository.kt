package no.nav.dagpenger.arena.trakt.db

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.Vedtak
import no.nav.dagpenger.arena.trakt.hendelser.Hendelse
import no.nav.dagpenger.arena.trakt.hendelser.VedtakHendelse
import no.nav.helse.rapids_rivers.RapidsConnection
import org.intellij.lang.annotations.Language

private val sikkerlogg = KotlinLogging.logger("tjenestekall.HendelseRepository")

internal class HendelseRepository(
    private val rapidsConnection: RapidsConnection,
) {
    companion object {
        internal fun fraVedtak(vedtak: Vedtak) = VedtakHendelse(vedtak)

        @Language("PostgreSQL")
        private const val LAGRE_QUERY = "INSERT INTO hendelse (melding_id) VALUES (?)"

        @Language("PostgreSQL")
        private const val VEDTAK_LINK_QUERY =
            "INSERT INTO hendelse_vedtak (melding_id, vedtak_id, oppdatert)\nVALUES (?, ?, ?)"
    }

    fun publiser(hendelse: Hendelse) {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.transaction {
                it.lagre(hendelse)
                rapidsConnection.publish(hendelse.message().toJson()).also {
                    sikkerlogg.info { "Publiserte nytt dagpengevedtak: ${hendelse.message().toJson()}" }
                }
            }
        }
    }

    private fun Session.lagre(hendelse: Hendelse) { // using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
        this.lagreHendelse(hendelse)
        this.lagreLink(hendelse)
    }

    private fun Session.lagreHendelse(hendelse: Hendelse) = this.run(queryOf(LAGRE_QUERY, hendelse.meldingId).asUpdate)

    private fun Session.lagreLink(hendelse: Hendelse) =
        when (hendelse) {
            is VedtakHendelse ->
                this.run(
                    queryOf(VEDTAK_LINK_QUERY, hendelse.meldingId, hendelse.vedtakId, hendelse.oppdatert).asExecute,
                )
            else -> throw IllegalArgumentException("Ukjent hendelsetype")
        }
}
