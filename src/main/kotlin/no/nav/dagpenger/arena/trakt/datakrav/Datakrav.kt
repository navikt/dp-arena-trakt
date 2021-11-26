package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder

internal abstract class Datakrav() {
    abstract val query: String

    abstract fun oppfyltFor(vedtak: Hendelse): Boolean

    internal fun finnData(params: Map<String, Any>, mapper: () -> (Row) -> String?): Boolean {
        val vedtaksfakta = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf(query, params).map(mapper()).asSingle)
        }
        return vedtaksfakta != null
    }
}
