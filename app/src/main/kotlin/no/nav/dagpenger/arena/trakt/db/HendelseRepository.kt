package no.nav.dagpenger.arena.trakt.db

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.Vedtak
import no.nav.dagpenger.arena.trakt.hendelser.Hendelse
import no.nav.dagpenger.arena.trakt.hendelser.VedtakHendelse
import no.nav.helse.rapids_rivers.RapidsConnection
import org.intellij.lang.annotations.Language

internal class HendelseRepository(private val rapidsConnection: RapidsConnection) {
    companion object {
        internal fun fraVedtak(vedtak: Vedtak) = VedtakHendelse(vedtak)

        @Language("PostgreSQL")
        private const val lagreQuery = "INSERT INTO hendelse (melding_id) VALUES (?)"

        @Language("PostgreSQL")
        private const val vedtakLinkQuery =
            "INSERT INTO hendelse_vedtak (melding_id, vedtak_id, oppdatert)\nVALUES (?, ?, ?)"
    }

    fun publiser(hendelse: Hendelse) {
        rapidsConnection.publish(hendelse.toJson())
        lagre(hendelse)
    }

    private fun lagre(hendelse: Hendelse) = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
        session.lagreHendelse(hendelse)
        session.lagreLink(hendelse)
    }

    private fun Session.lagreHendelse(hendelse: Hendelse) = this.run(queryOf(lagreQuery, hendelse.meldingId).asUpdate)

    private fun Session.lagreLink(hendelse: Hendelse) = when (hendelse) {
        is VedtakHendelse -> this.run(
            queryOf(vedtakLinkQuery, hendelse.meldingId, hendelse.vedtakId, hendelse.oppdatert).asExecute
        )
        else -> throw IllegalArgumentException("Ukjent hendelsetype")
    }
}
