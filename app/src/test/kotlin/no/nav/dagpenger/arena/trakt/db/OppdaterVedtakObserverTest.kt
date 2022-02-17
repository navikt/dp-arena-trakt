package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.BEREGNINGSLEDD_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.SAK_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.VEDTAKFAKTA_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.VEDTAK_TABELL
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
            dataRepository.lagre(sakJSON(dagpengeSakId, saksKode = ArenaKoder.DAGPENGE_SAK), tabell = SAK_TABELL)
            dataRepository.lagre(vedtakJSON(dagpengeVedtakId, dagpengeSakId), tabell = VEDTAK_TABELL)
            dataRepository.lagre(vedtaksfaktaJSON(dagpengeVedtakId), tabell = VEDTAKFAKTA_TABELL)
            dataRepository.lagre(beregningsleddJSON(dagpengeVedtakId), tabell = BEREGNINGSLEDD_TABELL)

            assertEquals(3, antallOppdateringerForVedtak(dagpengeVedtakId))
        }
    }

    @Test
    fun `Vedtak oppdateres ikke når sak ikke er knyttet til vedtaket`() {
        val ikkeDagpengeSakId = 1234
        val ikkeDagpengeVedtakId = 12345

        withMigratedDb {
            dataRepository.lagre(sakJSON(ikkeDagpengeSakId, saksKode = "AAP"), tabell = SAK_TABELL)
            dataRepository.lagre(vedtakJSON(ikkeDagpengeVedtakId, ikkeDagpengeSakId), tabell = VEDTAK_TABELL)
            dataRepository.lagre(vedtaksfaktaJSON(ikkeDagpengeVedtakId), tabell = VEDTAKFAKTA_TABELL)
            dataRepository.lagre(beregningsleddJSON(ikkeDagpengeVedtakId), tabell = BEREGNINGSLEDD_TABELL)

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
