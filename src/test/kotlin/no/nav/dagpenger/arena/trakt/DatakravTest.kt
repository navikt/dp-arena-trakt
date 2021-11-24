package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.BeregningsleddRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class DatakravTest {
    private val beregningsleddRepository = BeregningsleddRepository()

    private val beregningsledd = Beregningsledd("DPTEL", beregningsleddRepository)
    private val vedtak = Vedtak("872397432", beregningsledd)

    @Test
    fun `Er ikke oppfylt`() {
        assertFalse(beregningsledd.oppfyltFor(vedtak))
    }

    @Test
    fun `Er oppfylt`() {
        withMigratedDb {
            BeregningsleddRepository().insert(JSON)
            assertFalse(beregningsledd.oppfyltFor(vedtak))
        }
    }
}

private val JSON = """{
  "table": "SIAMO.BEREGNINGSLEDD",
  "op_type": "I",
  "op_ts": "2021-11-18 11:25:45.338291",
  "current_ts": "2021-11-18 11:57:59.252008",
  "pos": "00000000000003215801",
  "after": {
    "BEREGNINGSLEDD_ID": 232916770,
    "BEREGNINGSLEDDKODE": "DPTEL",
    "DATO_FRA": "2018-06-10 00:00:00",
    "PERSON_ID": 4785892,
    "DATO_TIL": null,
    "TABELLNAVNALIAS_KILDE": "VEDTAK",
    "OBJEKT_ID_KILDE": 872397432,
    "REG_USER": "JD4402",
    "REG_DATO": "2021-02-27 20:10:20",
    "MOD_USER": "JD4402",
    "MOD_DATO": "2021-02-27 20:10:20",
    "VERDI": 0,
    "TILLEGGSKODE": null,
    "PARTISJON": null
  },
  "system_read_count": 0,
  "system_participating_services": [
    {
      "service": "dp-arena-trakt",
      "instance": "dp-arena-trakt-7bd588b78d-nf5c9",
      "time": "2021-11-24T09:36:53.467315483"
    }
  ]
}""".trimMargin()
