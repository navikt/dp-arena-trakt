package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.lagre
import no.nav.dagpenger.arena.trakt.helpers.testHendelse
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import no.nav.dagpenger.arena.trakt.serde.VedtakHendelseJsonBuilder
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
            val vedtak = testHendelse(vedtaksid)

            assertFalse(vedtak.alleDatakravOppfylt())
        }
    }

    @Test
    fun `Hendelse skal være komplett`() {
        Postgres.withMigratedDb {
            val vedtak = testHendelse(vedtaksid)

            dataRepository.lagre(beregningsleddJSON(kode = "BL1"))
            dataRepository.lagre(vedtaksfaktaJSON(kode = "VF1"))
            dataRepository.lagre(vedtakJSON(vedtaksid.toInt()))
            assertTrue(vedtak.alleDatakravOppfylt())
        }
    }

    @Test
    fun `Hendelse skal være json`() {
        Postgres.withMigratedDb {
            val vedtak = testHendelse((vedtaksid))

            dataRepository.lagre(beregningsleddJSON(kode = "BL1"))
            dataRepository.lagre(vedtaksfaktaJSON(kode = "VF1"))
            dataRepository.lagre(vedtakJSON(vedtaksid.toInt()))

            VedtakHendelseJsonBuilder(vedtak).resultat().also { json ->
                assertTrue(json.has("@event_type"))
                assertTrue(json.has("vedtakId"))
                assertTrue(json.has("fakta"))
                assertEquals(listOf("BL1", "VF1", "utfall"), json["fakta"].map { it["id"].asText() })
            }
        }
    }
}
