package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.DAGPENGE_SAK
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.lagre
import no.nav.dagpenger.arena.trakt.helpers.sakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class DataRepositoryTest {

    private val dataRepository = DataRepository()

    @Test
    fun `Kan lagre JSON blobber som kommer fra Arena`() {
        withMigratedDb {
            dataRepository.lagre(beregningsleddJSON(kode = "BL1"))
            dataRepository.lagre(vedtaksfaktaJSON(kode = "VF1"))
            assertEquals(2, antallRaderMedData())
        }
    }

    @Test
    fun `Lagre returnerer primærnøkkelen til nylig inserted element`() {
        withMigratedDb {
            val generertPrimærnøkkel = dataRepository.lagre(sakJSON(456, DAGPENGE_SAK))
            assertEquals(1, generertPrimærnøkkel)
        }
    }

    @Test
    fun `Duplikat data ignoreres`() {
        val pos = UUID.randomUUID().toString()
        val skjedde = LocalDateTime.now()

        withMigratedDb {
            val primærNøkkel = dataRepository.lagre("SIAMO.VEDTAK", pos, LocalDateTime.now(), skjedde, vedtakJSON())
            assertEquals(1, primærNøkkel)
            val ignorert = dataRepository.lagre("SIAMO.VEDTAK", pos, LocalDateTime.now(), skjedde, vedtakJSON())
            assertNull(ignorert)
            assertEquals(1, antallRaderMedData())
        }
    }

    @Test
    fun `DpSak lagres, vurderes deretter til sletting, DpSak blir ikke slettet`() {
        withMigratedDb {
            val dpSak = 456
            dataRepository.lagre(sakJSON(dpSak, saksKode = DAGPENGE_SAK))
            assertEquals(1, antallRaderMedData())
        }
    }

    @Test
    fun `Finn alle relevante rader tilknyttet ett gitt vedtak`() {
        withMigratedDb {
            val vedtakId = 123
            assertEquals(0, dataRepository.hentVedtaksdata(vedtakId).size)

            dataRepository.lagre(beregningsleddJSON(vedtakId))
            dataRepository.lagre(vedtaksfaktaJSON(vedtakId))
            dataRepository.lagre(vedtakJSON(vedtakId, 5))
            dataRepository.lagre(vedtakJSON(15345, 53))

            assertEquals(3, dataRepository.hentVedtaksdata(vedtakId).size)
        }
    }

    private fun antallRaderMedData() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("SELECT COUNT(id) FROM arena_data WHERE data is not null").map { it.int(1) }.asSingle)
        }
}
