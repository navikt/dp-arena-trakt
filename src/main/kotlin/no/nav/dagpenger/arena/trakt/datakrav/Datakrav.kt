package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.IverksattVedtak
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder

internal abstract class Datakrav() {
    abstract val query: String

    abstract fun oppfyltFor(vedtak: IverksattVedtak): Boolean

    internal fun finnData(params: Map<String, Any>): Boolean {
        val vedtaksfakta = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(query, params)
                    .map { row ->
                        row.string("id")
                    }.asSingle
            )
        }
        return vedtaksfakta != null
    }
}
