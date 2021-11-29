package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.Hendelse.Type.VedtakIverksatt
import no.nav.dagpenger.arena.trakt.datakrav.Beregningsledd
import no.nav.dagpenger.arena.trakt.datakrav.Vedtak
import no.nav.dagpenger.arena.trakt.datakrav.Vedtaksfakta
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres
import no.nav.dagpenger.arena.trakt.helpers.VedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import no.nav.dagpenger.arena.trakt.serde.VedtakIverksattJsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HendelseTest {
    private val vedtaksid = "123"
    private val dataRepository = DataRepository()

    @Test
    fun `Hendelse skal ikke være komplett`() {
        Postgres.withMigratedDb {
            val vedtak = Hendelse(
                VedtakIverksatt,
                vedtaksid
            ) {
                krev(Beregningsledd("DPTEL"))
                krev(Vedtaksfakta("ENDRTILUNN"))
            }

            assertFalse(vedtak.komplett())
        }
    }

    @Test
    fun `Hendelse skal være komplett`() {
        Postgres.withMigratedDb {
            val vedtak = Hendelse(
                VedtakIverksatt,
                vedtaksid
            ) {
                krev(Beregningsledd("DPTEL"))
                krev(Vedtaksfakta("ENDRTILUNN"))
                krev(Vedtak(vedtaksid))
            }

            dataRepository.lagre(beregningsleddJSON("DPTEL"))
            dataRepository.lagre(vedtaksfaktaJSON("ENDRTILUNN"))
            dataRepository.lagre(VedtakJSON)

            assertTrue(vedtak.komplett())
        }
    }

    @Test
    fun `Hendelse skal være json`() {
        Postgres.withMigratedDb {
            val vedtak = Hendelse(
                VedtakIverksatt,
                vedtaksid
            ) {
                krev(Beregningsledd("DPTEL"))
                krev(Vedtaksfakta("ENDRTILUNN"))
                krev(Vedtak(vedtaksid))
            }

            dataRepository.lagre(beregningsleddJSON("DPTEL"))
            dataRepository.lagre(vedtaksfaktaJSON("ENDRTILUNN"))
            dataRepository.lagre(VedtakJSON)

            VedtakIverksattJsonBuilder(vedtak).resultat().also { json ->
                assertTrue(json.has("hendelse"))
                assertTrue(json.has("vedtakId"))
                assertTrue(json.has("fakta"))
                assertEquals(listOf("DPTEL", "ENDRTILUNN", "utfall"), json["fakta"].map { it["id"].asText() })
            }
        }
    }
}

