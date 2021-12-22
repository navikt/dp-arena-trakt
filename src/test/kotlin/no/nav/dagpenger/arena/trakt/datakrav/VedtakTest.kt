package no.nav.dagpenger.arena.trakt.datakrav

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.testHendelse
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtakTest {
    private val repository = DataRepository()
    private val vedtakHendelse = testHendelse("123")
    private val vedtak = Vedtak("123").apply { hendelse = vedtakHendelse.hendelseId }

    @Test
    fun `Vedtak finnes ikke enda`() {
        withMigratedDb {
            assertFalse(vedtak.oppfylt())
        }
    }

    @Test
    fun `Vedtak finnes`() {
        withMigratedDb {
            repository.lagre(vedtakJSON())
            assertTrue(vedtak.oppfylt())
        }
    }

    @Test
    fun `Spørringen treffer indeks`() {
        withMigratedDb {
            val vedtak = Vedtak("a")
            vedtak.hendelse = Hendelse.HendelseId(Hendelse.Type.Vedtak, "123")
            val query = vedtak.query
            val where = vedtak.params()
            val plan = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
                session.run(
                    queryOf("EXPLAIN ANALYZE $query", where).map {
                        it.string(1)
                    }.asList
                )
            }

            assertFalse(plan[0].contains("Seq Scan"))
        }
    }
}
