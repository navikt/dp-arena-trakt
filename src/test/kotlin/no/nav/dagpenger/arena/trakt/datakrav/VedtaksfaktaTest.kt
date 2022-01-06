package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.lagre
import no.nav.dagpenger.arena.trakt.helpers.testHendelse
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtaksfaktaTest {
    private val repository = DataRepository()
    private val vedtak = testHendelse("123")
    private val vedtaksfakta = Vedtaksfakta("ENDRTILUNN").apply { hendelse = vedtak.hendelseId }

    @Test
    fun `Vedtaksfaktakrav er ikke oppfylt`() {
        withMigratedDb {
            assertFalse(vedtaksfakta.oppfylt())
        }
    }

    @Test
    fun `Vedtaksfaktakrav er oppfylt`() {
        withMigratedDb {
            repository.lagre(vedtaksfaktaJSON())
            assertTrue(vedtaksfakta.oppfylt())
        }
    }

    @Test
    fun `Spørringen treffer indeks`() {
        withMigratedDb {
            val vedtaksfakta = Vedtaksfakta("a")
            vedtaksfakta.hendelse = Hendelse.HendelseId(Hendelse.Type.Vedtak, "123")
            val query = vedtaksfakta.query
            val where = vedtaksfakta.params()
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
