package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.lagre
import no.nav.dagpenger.arena.trakt.helpers.sakJSON
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SlettUønsketYtelseObserverTest {
    private val dataRepository = DataRepository().apply {
        addObserver(DataRepository.SlettUønsketYtelseObserver(this))
    }

    @Test
    fun `Ikke DpSak lagres, vurderes deretter til sletting, blir slettet`() {
        withMigratedDb {
            val aapSak = 456
            dataRepository.lagre(sakJSON(aapSak, saksKode = "AAP"))
            assertEquals(0, antallRaderMedData())
        }
    }

    private fun antallRaderMedData() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("SELECT COUNT(id) FROM arena_data WHERE data is not null").map { it.int(1) }.asSingle)
        }
}
