package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.Hendelse.Type.VedtakIverksatt
import no.nav.dagpenger.arena.trakt.datakrav.Beregningsledd
import no.nav.dagpenger.arena.trakt.datakrav.Vedtaksfakta
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.helpers.BeregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.Postgres
import no.nav.dagpenger.arena.trakt.helpers.VedtaksFaktaJSON
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HendelseTest {
    private val vedtaksid = "123"
    private val dataRepository = DataRepository()

    @Test
    fun `Vedtak skal ikke være komplett`() {
        Postgres.withMigratedDb {
            val vedtak = Hendelse(
                VedtakIverksatt,
                vedtaksid,
                Beregningsledd("DPTEL"),
                Vedtaksfakta("ANTB")
            )
            assertFalse(vedtak.komplett())
        }
    }

    @Test
    fun `Vedtak skal være komplett`() {
        Postgres.withMigratedDb {
            val vedtak = Hendelse(
                VedtakIverksatt,
                vedtaksid,
                Beregningsledd("DPTEL"),
                Vedtaksfakta("ENDRTILUNN")
            )

            dataRepository.lagre(BeregningsleddJSON)
            dataRepository.lagre(VedtaksFaktaJSON)
            assertTrue(vedtak.komplett())
        }
    }
}
