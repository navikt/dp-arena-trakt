package no.nav.dagpenger.arena.trakt.helpers

import no.nav.dagpenger.arena.trakt.Vedtak
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal fun vedtak(vedtakId: Int = 1, sakId: Int = 1) = Vedtak(
    sakId = sakId,
    vedtakId = vedtakId,
    personId = 1,
    vedtaktypekode = "O",
    utfallkode = "JA",
    rettighetkode = "DAGO",
    vedtakstatuskode = "IVERK",
    opprettet = LocalDateTime.now(),
    oppdatert = LocalDateTime.now(),
)

@Language("JSON")
internal fun vedtaksfaktaJSON(vedtakId: Int = 123, kode: String = "ENDRTILUNN") = """{
  "table": "SIAMO.VEDTAKFAKTA",
  "op_type": "I",
  "op_ts": "2021-11-18 11:37:16.455389",
  "current_ts": "2021-11-18 12:54:59.322016",
  "pos": "00000000000002519525",
  "after": {
    "VEDTAK_ID": $vedtakId,
    "VEDTAKFAKTAKODE": "$kode",
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
}
""".trimMargin()

@Language("JSON")
internal fun beregningsleddJSON(vedtakId: Int = 123, kode: String = "DPTEL") = """{
  "table": "SIAMO.BEREGNINGSLEDD",
  "op_type": "I",
  "op_ts": "2021-11-18 11:25:45.338291",
  "current_ts": "2021-11-18 11:57:59.252008",
  "pos": "00000000000003215801",
  "after": {
    "BEREGNINGSLEDD_ID": 232916770,
    "BEREGNINGSLEDDKODE": "$kode",
    "DATO_FRA": "2018-06-10 00:00:00",
    "PERSON_ID": 4785892,
    "DATO_TIL": null,
    "TABELLNAVNALIAS_KILDE": "VEDTAK",
    "OBJEKT_ID_KILDE": $vedtakId,
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
}
""".trimMargin()

private val arenaDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

@Language("JSON")
internal fun vedtakJSON(
    vedtakId: Int = 123,
    sakId: Int = 12345,
    opType: String = "I",
    pos: String = "00000000000019642427",
    status: String = "IVERK",
    modDato: LocalDateTime = LocalDateTime.parse("2019-09-27 04:04:26", arenaDateFormatter)
) = """{
  "table": "SIAMO.VEDTAK",
  "op_type": "$opType",
  "op_ts": "2021-11-18 11:36:18.004455",
  "current_ts": "2021-11-18 12:53:14.964000",
  "pos": "$pos",
  "after": {
    "VEDTAK_ID": $vedtakId,
    "SAK_ID": $sakId,
    "VEDTAKSTATUSKODE": "$status",
    "VEDTAKTYPEKODE": "O",
    "REG_DATO": "2019-09-27 04:04:26",
    "REG_USER": "ANB1243",
    "MOD_DATO": "${modDato.format(arenaDateFormatter)}",
    "MOD_USER": "AGH1243",
    "UTFALLKODE": "JA",
    "BEGRUNNELSE": "Syntetisert rettighet",
    "BRUKERID_ANSVARLIG": "ANB1243",
    "AETATENHET_BEHANDLER": "1243",
    "AAR": 2019,
    "LOPENRSAK": 580493,
    "LOPENRVEDTAK": 1,
    "RETTIGHETKODE": "PERM",
    "AKTFASEKODE": "IKKE",
    "BREV_ID": 30822341,
    "TOTALBELOP": null,
    "DATO_MOTTATT": "2019-09-27 00:00:00",
    "VEDTAK_ID_RELATERT": null,
    "AVSNITTLISTEKODE_VALGT": null,
    "PERSON_ID": 4795335,
    "BRUKERID_BESLUTTER": "AGH1243",
    "STATUS_SENSITIV": null,
    "VEDLEGG_BETPLAN": "N",
    "PARTISJON": null,
    "OPPSUMMERING_SB2": null,
    "DATO_UTFORT_DEL1": null,
    "DATO_UTFORT_DEL2": null,
    "OVERFORT_NAVI": null,
    "FRA_DATO": "2019-10-07 00:00:00",
    "TIL_DATO": null,
    "SF_OPPFOLGING_ID": null,
    "STATUS_SOSIALDATA": "N",
    "KONTOR_SOSIALDATA": null,
    "TEKSTVARIANTKODE": null,
    "VALGT_BESLUTTER": "AGH1243",
    "TEKNISK_VEDTAK": null,
    "DATO_INNSTILT": "2021-03-27 04:04:26",
    "ER_UTLAND": "N"
  },
  "system_read_count": 0,
  "system_participating_services": [
    {
      "service": "dp-arena-trakt",
      "instance": "dp-arena-trakt-84b5487744-zmc97",
      "time": "2021-11-25T15:40:57.301038008"
    }
  ]
}
""".trimMargin()

@Language("JSON")
internal fun sakJSON(sakId: Int = 12345, saksKode: String = "AAP") = """{
  "table": "SIAMO.SAK",
  "op_type": "I",
  "op_ts": "2021-11-18 11:25:45.338291",
  "current_ts": "2021-11-18 11:57:59.252008",
  "pos": "00000000000003215801",
  "after": {
    "SAK_ID": $sakId,
    "SAKSKODE": "$saksKode",
    "REG_DATO": "2016-07-12 15:00:23",
    "REG_USER": "ARBLINJE",
    "MOD_DATO": "2019-12-28 14:10:26",
    "MOD_USER": "GRENSESN",
    "TABELLNAVNALIAS": "PERS",
    "OBJEKT_ID": 3894300,
    "AAR": 2016,
    "LOPENRSAK": 408776,
    "DATO_AVSLUTTET": null,
    "SAKSTATUSKODE": "INAKT",
    "ARKIVNOKKEL": null,
    "AETATENHET_ARKIV": null,
    "ARKIVHENVISNING": null,
    "BRUKERID_ANSVARLIG": "ED4401",
    "AETATENHET_ANSVARLIG": "4450",
    "OBJEKT_KODE": null,
    "STATUS_ENDRET": "2018-06-05 02:07:07",
    "PARTISJON": null,
    "ER_UTLAND": "N"
  },
  "system_read_count": 0,
  "system_participating_services": [
    {
      "service": "dp-arena-trakt",
      "instance": "dp-arena-trakt-7bd588b78d-nf5c9",
      "time": "2021-11-24T09:36:53.467315483"
    }
  ]
}
""".trimMargin()

@Language("JSON")
internal fun nyRettighetVedtakJSON(vedtakId: Int = 123, sakId: Int = 12345) = """{
  "table": "SIAMO.VEDTAK",
  "op_type": "I",
  "op_ts": "2021-11-18 11:36:18.004455",
  "current_ts": "2021-11-18 12:53:14.964000",
  "pos": "00000000000019642427",
  "after": {
    "VEDTAK_ID": $vedtakId,
    "SAK_ID": $sakId,
    "VEDTAKSTATUSKODE": "IVERK",
    "VEDTAKTYPEKODE": "O",
    "REG_DATO": "2019-09-27 04:04:26",
    "REG_USER": "ANB1243",
    "MOD_DATO": "2019-09-27 04:04:26",
    "MOD_USER": "AGH1243",
    "UTFALLKODE": "JA",
    "BEGRUNNELSE": "Syntetisert rettighet",
    "BRUKERID_ANSVARLIG": "ANB1243",
    "AETATENHET_BEHANDLER": "1243",
    "AAR": 2019,
    "LOPENRSAK": 580493,
    "LOPENRVEDTAK": 1,
    "RETTIGHETKODE": "DAGO",
    "AKTFASEKODE": "IKKE",
    "BREV_ID": 30822341,
    "TOTALBELOP": null,
    "DATO_MOTTATT": "2019-09-27 00:00:00",
    "VEDTAK_ID_RELATERT": null,
    "AVSNITTLISTEKODE_VALGT": null,
    "PERSON_ID": 4795335,
    "BRUKERID_BESLUTTER": "AGH1243",
    "STATUS_SENSITIV": null,
    "VEDLEGG_BETPLAN": "N",
    "PARTISJON": null,
    "OPPSUMMERING_SB2": null,
    "DATO_UTFORT_DEL1": null,
    "DATO_UTFORT_DEL2": null,
    "OVERFORT_NAVI": null
    "FRA_DATO": "2019-10-07 00:00:00",
    "TIL_DATO": null,
    "SF_OPPFOLGING_ID": null,
    "STATUS_SOSIALDATA": "N",
    "KONTOR_SOSIALDATA": null,
    "TEKSTVARIANTKODE": null,
    "VALGT_BESLUTTER": "AGH1243",
    "TEKNISK_VEDTAK": null,
    "DATO_INNSTILT": "2021-03-27 04:04:26",
    "ER_UTLAND": "N"
  },
  "system_read_count": 0,
  "system_participating_services": [
    {
      "service": "dp-arena-trakt",
      "instance": "dp-arena-trakt-84b5487744-zmc97",
      "time": "2021-11-25T15:40:57.301038008"
    }
  ]
}
""".trimMargin()
