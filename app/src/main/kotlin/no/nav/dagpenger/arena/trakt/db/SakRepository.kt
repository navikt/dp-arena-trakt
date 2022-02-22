package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.tjenester.SakService.Sak
import org.intellij.lang.annotations.Language

internal class SakRepository {
    companion object {
        @Language("PostgreSQL")
        private val lagreQuery = """
            |INSERT INTO sak (sak_id,
            |                 er_dagpenger)
            |VALUES (?, ?)
            |ON CONFLICT (sak_id) DO NOTHING
        """.trimMargin()
    }

    val observers = listOf<SakObserver>()

    fun lagre(sak: Sak): Int {
        return using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    lagreQuery,
                    sak.sakId,
                    sak.erDagpenger,
                ).asUpdate
            )
        }.also {
            observers.forEach { it.nySak(sak) }
        }
    }

    interface SakObserver {
        fun nySak(sak: Sak) {}
    }
}
