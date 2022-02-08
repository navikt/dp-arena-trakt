package no.nav.dagpenger.arena.trakt.db

import com.fasterxml.jackson.databind.ObjectMapper
import kotliquery.Session
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
        |ON CONFLICT DO NOTHING
        |RETURNING id""".trimMargin()

    fun lagre(tabell: String, pos: String, skjedde: LocalDateTime, replikert: LocalDateTime, json: String): Int? =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    lagreQuery,
                    tabell,
                    pos,
                    skjedde,
                    replikert,
                    json
                ).map { it.int("id") }.asSingle
            )
        }.also { observers.forEach { it.nyData() } } // TODO: Det er kun ny data dersom it != null?

    internal fun slettDataSomIkkeOmhandlerDagpenger(): List<Int> {
        return using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            val iderTilSletting = hentRaderSomSkalSlettes(session)
            slettDataSomIkkeOmhandlerDagpenger(session, iderTilSletting)
        }
    }

    private fun hentRaderSomSkalSlettes(session: Session) = session.run(
        queryOf("SELECT id, data FROM arena_data WHERE behandlet is NULL ORDER BY id ASC").map {
            if (erDagpenger(it.string("data")) == false) listOf(it.int("id")) else null
        }.asList
    )

    private fun slettDataSomIkkeOmhandlerDagpenger(session: Session, iderTilSletting: List<List<Int>>) =
        session.batchPreparedStatement("UPDATE arena_data SET data=null, behandlet=now() WHERE id=?", iderTilSletting)

    internal fun slettRadSomIkkeOmhandlerDagpenger(primærnøkkel: Int?) {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            val data = hentData(session, primærnøkkel)

            if (erDagpenger(data) == false) {
                session.run(
                    queryOf("UPDATE arena_data SET data=null, behandlet=now() WHERE id=?", primærnøkkel).asExecute
                )
            }
        }
    }

    private fun hentData(session: Session, primærnøkkel: Int?): String? = session.run(
        queryOf("SELECT data FROM arena_data WHERE id=?", primærnøkkel).map {
            it.string("data")
        }.asSingle
    )
}

private fun erDagpenger(data: String?): Boolean? {
    if (data == null) return null

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
