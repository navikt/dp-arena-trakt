package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class SletterutineTest {

    @Test
    fun `Sletterutine spørringen som henter rader treffer indeks`() {
        withMigratedDb {
            genererDataFraUkjentYtelse(2000)
            val query = DataRepository.finnRaderTilSlettingQuery
            val batchsize = 200
            val plan: String = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
                session.run(
                    queryOf("EXPLAIN ANALYZE $query", batchsize).map {
                        it.string(1)
                    }.asList
                )
            }.joinToString("\n")

            assertTrue(plan.contains("Bitmap Index Scan on i_behandlet"))
        }
    }

    private fun genererDataFraUkjentYtelse(antallRader: Int, json: String = vedtaksfaktaJSON()) {
        for (i in 1..antallRader) {
            lagreData(json)
        }
    }

    private fun lagreData(json: String) {
        @Language("PostgreSQL")
        val lagreQuery = """INSERT INTO arena_data (tabell, pos, skjedde, replikert, data)
            |VALUES (?, ?, ?, ?, ?::jsonb)
            |ON CONFLICT DO NOTHING
            |""".trimMargin()

        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    lagreQuery,
                    "SIAMO.SAK",
                    UUID.randomUUID().toString(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    json
                ).asExecute
            )
        }
    }
}
