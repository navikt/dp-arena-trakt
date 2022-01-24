package no.nav.dagpenger.arena.trakt.tjenester

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.db.ArenaMottakRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DataMottakServiceTest {
    private val dataRepository = ArenaMottakRepository(2)
    private val rapid = TestRapid().also {
        DataMottakService(it, dataRepository)
    }

    @BeforeEach
    internal fun setup() {
        rapid.reset()
    }

    @Test
    fun `Tar imot JSON og lagrer den`() {
        withMigratedDb {
            rapid.sendTestMessage(vedtaksfaktaJSON("FDATO"))
            rapid.sendTestMessage(vedtaksfaktaJSON("FDATO"))

            assertEquals(2, antallRader())
        }
    }

    @Test
    fun `Lagrer ved shutdown`() {
        withMigratedDb {
            rapid.sendTestMessage(vedtaksfaktaJSON("FDATO"))
            rapid.stop().also {
                // TestRapid kaller ikke notifyShutdown()
                dataRepository.stop()
            }

            assertThrows<IllegalStateException> {
                // Riveren vil ikke kunne ta lagre nye meldinger etter shutdown
                rapid.sendTestMessage(vedtaksfaktaJSON("FDATO"))
            }

            assertEquals(1, antallRader())
        }
    }

    private fun antallRader() =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("SELECT COUNT(id) FROM arena_data").map { it.int(1) }.asSingle)
        }
}

private var pos = 0

@Language("JSON")
internal fun vedtaksfaktaJSON(navn: String = "ENDRTILUNN", vedtakId: Int = 123): String {
    return """{
  "table": "SIAMO.VEDTAKFAKTA",
  "op_type": "I",
  "op_ts": "2021-11-18 11:37:16.455389",
  "current_ts": "2021-11-18 12:54:59.322016",
  "pos": "${pos++}",
  "after": {
    "VEDTAK_ID": $vedtakId,
    "VEDTAKFAKTAKODE": "$navn",
    "VEDTAKVERDI": "foobar",
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
}
