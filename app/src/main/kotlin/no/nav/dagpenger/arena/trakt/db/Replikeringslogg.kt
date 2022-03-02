package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.meldinger.ReplikeringsMelding
import no.nav.dagpenger.arena.trakt.meldinger.ReplikeringsMelding.ReplikeringsId
import org.intellij.lang.annotations.Language

internal class Replikeringslogg {
    companion object {
        @Language("PostgreSQL")
        private val lagreQuery = """
            |INSERT INTO replikeringslogg(replikering_id, operasjon, replikert, først_sett, beskrivelse)
            |VALUES (?, ?, ?, NOW(), ?)
            |ON CONFLICT (replikering_id) DO NOTHING
        """.trimMargin()

        @Language("PostgreSQL")
        private val markerSomBehandletQuery = """
            |UPDATE replikeringslogg SET behandlet=NOW()
            |WHERE replikering_id=?
        """.trimMargin()

        @Language("PostgreSQL")
        private val erBehandletQuery = """
            |SELECT behandlet FROM replikeringslogg
            |WHERE replikering_id=?
        """.trimMargin()
    }

    fun lagre(message: ReplikeringsMelding) {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    lagreQuery,
                    message.id.toString(),
                    message.operasjon,
                    message.operasjon_ts,
                    message.meldingBeskrivelse()
                ).asUpdate
            )
        }
    }

    fun markerSomBehandlet(id: ReplikeringsId) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf(markerSomBehandletQuery, id.toString()).asUpdate)
        }

    fun erBehandlet(id: ReplikeringsId): Boolean =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf(erBehandletQuery, id.toString()).map { it.localDateTimeOrNull("behandlet") }.asSingle)
        } != null
}
