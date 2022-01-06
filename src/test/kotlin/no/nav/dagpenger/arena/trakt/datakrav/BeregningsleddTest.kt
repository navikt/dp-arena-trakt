package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.lagre
import no.nav.dagpenger.arena.trakt.helpers.testHendelse
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BeregningsleddTest {
    private val repository = DataRepository()
    private val vedtak = testHendelse("123")
    private val beregningsledd = Beregningsledd("DPTEL").apply { hendelse = vedtak.hendelseId }

    @Test
    fun `Beregningsleddkrav er ikke oppfylt`() {
        withMigratedDb {
            assertFalse(beregningsledd.oppfylt())
        }
    }

    @Test
    fun `Beregningsleddkrav er oppfylt`() {
        withMigratedDb {
            repository.lagre(beregningsleddJSON())
            assertTrue(beregningsledd.oppfylt())
        }
    }

    @Test
    fun `Spørringen treffer indeks`() {
        withMigratedDb {
            val beregningsledd = Beregningsledd("a")
            beregningsledd.hendelse = Hendelse.HendelseId(Hendelse.Type.Vedtak, "123")
            repository.lagre(beregningsleddJSON("a", 123))

            val query = beregningsledd.query
            val where = beregningsledd.params()
            val plan = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
                session.run(
                    queryOf("EXPLAIN ANALYZE $query", where).map {
                        it.string(1)
                    }.asList
                )
            }

            assertTrue(plan[0].contains("Index Scan"))
        }
    }
}
