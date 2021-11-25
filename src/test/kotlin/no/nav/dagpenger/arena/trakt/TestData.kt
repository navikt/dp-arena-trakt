package no.nav.dagpenger.arena.trakt

internal val VedtaksFaktaJSON = """{
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

internal val BeregningsleddJSON = """{
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
    "OBJEKT_ID_KILDE": 123,
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
