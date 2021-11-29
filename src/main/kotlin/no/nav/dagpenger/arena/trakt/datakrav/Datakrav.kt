package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.arena.trakt.serde.DatakravVisitor

internal abstract class Datakrav<T>(private val id: String) {
    internal lateinit var hendelse: Hendelse

    internal abstract fun params(): Map<String, Any>
    internal abstract val query: String
    private val data get() = finnData()

    fun oppfylt() = data !== null

    internal abstract fun mapper(row: Row): T

    private fun finnData() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf(query, params()).map(::mapper).asSingle)
        }

    fun accept(visitor: DatakravVisitor) {
        visitor.visit(this, id, data)
    }
}
