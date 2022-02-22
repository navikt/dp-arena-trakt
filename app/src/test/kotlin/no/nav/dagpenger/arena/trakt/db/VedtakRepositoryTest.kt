package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.db.VedtakRepository.VedtakObserver
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.vedtak
import no.nav.dagpenger.arena.trakt.tjenester.SakSink.Sak
import no.nav.dagpenger.arena.trakt.tjenester.VedtakSink.Vedtak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VedtakRepositoryTest {
    private val sakRepository = SakRepository()
    private val repository = VedtakRepository(sakRepository)

    @Test
    fun `lagrer vedtak`() {
        withMigratedDb {
            assertEquals(1, repository.lagre(vedtak()))
        }
    }

    @Test
    fun `Varsler om nytt vedtak når vi saken er dagpegner`() {
        withMigratedDb {
            sakRepository.lagre(Sak(1, true))
            repository.leggTilObserver(testObserver)
            repository.lagre(vedtak(1, 1))

            assertEquals(1, testObserver.nyeVedtak.size)
        }
    }

    @Test
    fun `Varsler ikke om nytt vedtak når saken ikke er dagpenger`() {
        withMigratedDb {
            sakRepository.lagre(Sak(1, false))
            repository.leggTilObserver(testObserver)
            repository.lagre(vedtak(1, 1))

            assertEquals(0, testObserver.nyeVedtak.size)
        }
    }

    @Test
    fun `Varsler om nye vedtak når en sak ankommer sent`() {
        withMigratedDb {
            repository.leggTilObserver(testObserver)
            repository.lagre(vedtak(1, 1))
            repository.lagre(vedtak(2, 1))
            repository.lagre(vedtak(3, 2))

            sakRepository.lagre(Sak(1, true))
            sakRepository.lagre(Sak(2, false))

            assertEquals(2, testObserver.nyeVedtak.size)
        }
    }

    @Test
    fun `Sletter vedtak som ikke er dagpenger når en sak ankommer sent`() {
        withMigratedDb {
            repository.leggTilObserver(testObserver)
            repository.lagre(vedtak(1, 1))
            repository.lagre(vedtak(2, 2))

            sakRepository.lagre(Sak(1, true))
            sakRepository.lagre(Sak(2, false))

            assertEquals(1, testObserver.nyeVedtak.size)
            assertEquals(1, antallVedtak())
            assertEquals(2, antallSaker())
        }
    }

    private fun antallSaker() = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
        session.run(queryOf("SELECT COUNT(1) AS antall FROM sak").map { it.int("antall") }.asSingle)
    }

    private fun antallVedtak() = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
        session.run(queryOf("SELECT COUNT(1) AS antall FROM vedtak").map { it.int("antall") }.asSingle)
    }

    private val testObserver = object : VedtakObserver {
        val nyeVedtak = mutableListOf<Vedtak>()

        override fun nyttDagpengeVedtak(vedtak: Vedtak) {
            nyeVedtak.add(vedtak)
        }
    }
}
