package no.nav.dagpenger.arena.trakt.db

import com.fasterxml.jackson.databind.ObjectMapper
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

internal class DataRepository private constructor(
    private val observers: MutableList<DataObserver>
) {
    constructor() : this(mutableListOf())

    fun addObserver(observer: DataObserver) = observers.add(observer)

    @Language("PostgreSQL")
    private val lagreQuery =
        """INSERT INTO arena_data (tabell, pos, skjedde, replikert, data)
        |VALUES (?, ?, ?, ?, ?::jsonb)
        |ON CONFLICT DO NOTHING""".trimMargin()

    fun lagre(tabell: String, pos: String, skjedde: LocalDateTime, replikert: LocalDateTime, json: String) {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    lagreQuery,
                    tabell,
                    pos,
                    skjedde,
                    replikert,
                    json
                ).asUpdate
            )
        }.also {
            // vurderSletting()
            observers.forEach { it.nyData() }
        }
    }

    internal fun rydd() {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            val iderTilSletting: List<List<Int>> = session.run(
                queryOf("SELECT id, data FROM arena_data WHERE behandlet is NULL ORDER BY id ASC").map {
                    if (erDagpenger(it.string("data")) == false) listOf(it.int("id")) else null
                }.asList
            )
            session.batchPreparedStatement(
                "UPDATE arena_data SET data=null, behandlet=now() WHERE id=?", iderTilSletting
            )
        }
    }
}

private fun erDagpenger(data: String): Boolean? {
    val json = ObjectMapper().readTree(data)
    val tabell = json["table"].asText()
    if (tabell == "SIAMO.SAK") {
        lagreSak(json["after"]["SAK_ID"].asInt(), json["after"]["SAKSKODE"].asText())
    }
    if (tabell == "SIAMO.VEDTAK") {
        lagreVedtak(json["after"]["VEDTAK_ID"].asInt(), json["after"]["SAK_ID"].asInt())
    }

    return when (tabell) {
        "SIAMO.SAK" -> json["after"]["SAKSKODE"].asText() == "DAGP"
        "SIAMO.VEDTAK" -> erDpVedtak(json["after"]["VEDTAK_ID"].asInt())
        "SIAMO.VEDTAKFAKTA" -> erDpVedtak(json["after"]["VEDTAK_ID"].asInt())
        "SIAMO.BEREGNINGSLEDD" -> if (json["after"]["TABELLNAVNALIAS_KILDE"].asText() == "VEDTAK") erDpVedtak(json["after"]["OBJEKT_ID_KILDE"].asInt()) else null
        else -> null
    }
}

private fun erDpVedtak(vedtakId: Int): Boolean? =
    using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
        session.run(
            queryOf(
                "SELECT er_dagpenger FROM sak LEFT JOIN vedtak ON sak.sak_id = vedtak.sak_id WHERE vedtak.vedtak_id = ?",
                vedtakId
            ).map {
                it.boolean("er_dagpenger")
            }.asSingle
        )
    }

private fun lagreSak(sakId: Int, saksKode: String) {
    val erDagpenger = saksKode == "DAGP"
    using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
        session.run(
            queryOf(
                "INSERT INTO sak (sak_id,er_dagpenger) VALUES(?,?) ON CONFLICT DO NOTHING", sakId, erDagpenger
            ).asUpdate
        )
    }
}

private fun lagreVedtak(vedtakId: Int, sakId: Int) {
    using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
        session.run(
            queryOf(
                "INSERT INTO vedtak (vedtak_id,sak_id) VALUES(?,?) ON CONFLICT DO NOTHING", vedtakId, sakId
            ).asUpdate
        )
    }
}

internal interface DataObserver {
    fun nyData() {}
}
