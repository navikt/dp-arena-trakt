package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.db.DataRepository.SlettUønsketYtelseObserver
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
    private val dataRepository = DataRepository().apply {
        addObserver(SlettUønsketYtelseObserver(this))
    }

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
            val generertPrimærnøkkel = dataRepository.lagre(sakJSON(456, "DAGP"))
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
            dataRepository.lagre(sakJSON(dpSak, saksKode = "DAGP"))
            assertEquals(1, antallRaderMedData())
        }
    }

    @Test
    fun `Ikke DpSak lagres, vurderes deretter til sletting, blir slettet`() {
        withMigratedDb {
            val aapSak = 456
            dataRepository.lagre(sakJSON(aapSak, saksKode = "AAP"))
            assertEquals(0, antallRaderMedData())
        }
    }

    @Test
    fun `Ikke dagpenge relaterte data skal slettes`() {
        withMigratedDb {
            val ikkeDpVedtak = 123
            val ikkeDpSak = 6789
            val dpVedtak = 127
            val dpSak = 456

            dataRepository.lagre(beregningsleddJSON(ikkeDpVedtak, kode = "IKKE_DP"))
            dataRepository.lagre(vedtaksfaktaJSON(ikkeDpVedtak, kode = "IKKE_DP"))
            dataRepository.lagre(vedtakJSON(ikkeDpVedtak, ikkeDpSak))
            dataRepository.lagre(vedtakJSON(dpVedtak, dpSak))
            dataRepository.lagre(sakJSON(dpSak, saksKode = "DAGP"))

            dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(10)

            assertEquals(5, antallRaderMedData())
            dataRepository.lagre(sakJSON(ikkeDpSak))
            dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(10)
            dataRepository.batchSlettDataSomIkkeOmhandlerDagpenger(10)
            assertEquals(2, antallRaderMedData())
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
