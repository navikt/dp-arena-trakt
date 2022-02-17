package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.db.DataRepository.OppdaterVedtakObserver
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.lagre
import no.nav.dagpenger.arena.trakt.helpers.sakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OppdaterVedtakObserverTest {

    private val dataRepository = DataRepository().apply {
        addObserver(OppdaterVedtakObserver(this))
    }

    @Test
    fun `Vedtak oppdateres ved ny data knyttet til vedtaket`() {
        val dagpengeSakId = 1234
        val dagpengeVedtakId = 12345

        withMigratedDb {
            dataRepository.lagre(sakJSON(dagpengeSakId, saksKode = ArenaKoder.DAGPENGE_SAK), tabell = "SIAMO.SAK")
            dataRepository.lagre(vedtakJSON(dagpengeVedtakId, dagpengeSakId), tabell = "SIAMO.VEDTAK")
            dataRepository.lagre(vedtaksfaktaJSON(dagpengeVedtakId), tabell = "SIAMO.VEDTAKFAKTA")
            dataRepository.lagre(beregningsleddJSON(dagpengeVedtakId), tabell = "SIAMO.BEREGNINGSLEDD")

            assertEquals(3, antallOppdateringerForVedtak(dagpengeVedtakId))
        }
    }

    @Test
    fun `Vedtak oppdateres ikke når sak ikke er knyttet til vedtaket`() {
        val ikkeDagpengeSakId = 1234
        val ikkeDagpengeVedtakId = 12345

        withMigratedDb {
            dataRepository.lagre(sakJSON(ikkeDagpengeSakId, saksKode = "AAP"), tabell = "SIAMO.SAK")
            dataRepository.lagre(vedtakJSON(ikkeDagpengeVedtakId, ikkeDagpengeSakId), tabell = "SIAMO.VEDTAK")
            dataRepository.lagre(vedtaksfaktaJSON(ikkeDagpengeVedtakId), tabell = "SIAMO.VEDTAKFAKTA")
            dataRepository.lagre(beregningsleddJSON(ikkeDagpengeVedtakId), tabell = "SIAMO.BEREGNINGSLEDD")

            assertEquals(0, antallOppdateringerForVedtak(ikkeDagpengeVedtakId))
        }
    }

    private fun antallOppdateringerForVedtak(vedtakId: Int) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf("SELECT antall_oppdateringer FROM vedtak WHERE vedtak_id=?", vedtakId).map {
                    it.int("antall_oppdateringer")
                }.asSingle
            )
        }
}
