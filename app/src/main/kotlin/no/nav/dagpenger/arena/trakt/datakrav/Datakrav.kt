package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.Hendelse.HendelseId
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.arena.trakt.serde.DatakravVisitor
import java.math.BigInteger

internal abstract class Datakrav<T>(private val kode: String) {
    internal lateinit var hendelse: HendelseId

    internal abstract fun params(): Map<String, Any>
    internal abstract val query: String
    private val data get() = finnData()

    fun oppfylt() = data !== null

    internal abstract fun mapper(row: Row): Resultat<T>
    private fun finnData() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf(query, params()).map(::mapper).asSingle)
        }

    fun accept(visitor: DatakravVisitor) {
        visitor.visit(this, kode, data, oppfylt())
    }

    internal data class Resultat<T>(val id: BigInteger, val data: T)
}

internal enum class ArenaKode(val arenaKode: String) {
    DAGPENGE_PERIODE_TELLER("DPTEL"),
    GJELDER_FRA_DATO("FDATO")
}
