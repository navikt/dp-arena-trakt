package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.Sak
import org.intellij.lang.annotations.Language

internal class SakRepository private constructor(
    private val observers: MutableList<SakObserver>
) {
    constructor() : this(mutableListOf<SakObserver>())

    companion object {
        @Language("PostgreSQL")
        private val lagreQuery = """
            |INSERT INTO sak (sak_id,
            |                 er_dagpenger, opprettet, oppdatert)
            |VALUES (?, ?, ?, ?)
            |ON CONFLICT (sak_id) DO NOTHING
        """.trimMargin()
    }

    fun leggTilObserver(observer: SakObserver) = observers.add(observer)

    fun lagre(sak: Sak): Int {
        return using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    lagreQuery,
                    sak.sakId,
                    sak.erDagpenger,
                    sak.opprettet,
                    sak.oppdatert
                ).asUpdate
            )
        }.also {
            observers.forEach { it.nySak(sak) }
        }
    }

    fun erDagpenger(sakId: Int) = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
        session.run(
            queryOf("SELECT er_dagpenger AS er_dagpenger FROM sak WHERE sak_id = ?", sakId).map {
                it.boolean("er_dagpenger")
            }.asSingle
        )
    }

    interface SakObserver {
        fun nySak(sak: Sak) {}
    }
}
