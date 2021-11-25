package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.VedtaksfaktaRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtaksfaktaTest {
    private val repository = VedtaksfaktaRepository()
    private val vedtaksfakta = Vedtaksfakta("ENDRTILUNN", repository)
    private val vedtak = Vedtak("123", vedtaksfakta)

    @Test
    fun `Vedtaksfaktakrav er ikke oppfylt`() {
        withMigratedDb {
            assertFalse(vedtaksfakta .oppfyltFor(vedtak))
        }
    }

    @Test
    fun `Vedtaksfaktakrav er oppfylt`() {
        withMigratedDb {
            VedtaksfaktaRepository().insert(JSON)
            assertTrue(vedtaksfakta.oppfyltFor(vedtak))
        }
    }


}

private val JSON = """{
  "table": "SIAMO.VEDTAKFAKTA",
  "op_type": "I",
  "op_ts": "2021-11-18 11:37:16.455389",
  "current_ts": "2021-11-18 12:54:59.322016",
  "pos": "00000000000002519525",
  "after": {
    "VEDTAK_ID": 123,
    "VEDTAKFAKTAKODE": "ENDRTILUNN",
    "VEDTAKVERDI": null,
    "REG_DATO": "2020-07-25 13:04:24",
    "REG_USER": "AT4402",
    "MOD_DATO": "2020-07-25 13:04:24",
    "MOD_USER": "AT4402",
    "PERSON_ID": null,
    "PARTISJON": null
  },
  "system_read_count": 0,
  "system_participating_services": [
    {
      "service": "dp-arena-trakt",
      "instance": "dp-arena-trakt-7bd588b78d-nf5c9",
      "time": "2021-11-24T09:36:54.56467414"
    }
  ]
}""".trimMargin()
