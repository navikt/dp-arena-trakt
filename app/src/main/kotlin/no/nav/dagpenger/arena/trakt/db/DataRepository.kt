package no.nav.dagpenger.arena.trakt.db

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.db.DataRepository.DataObserver.NyDataEvent
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val logg = KotlinLogging.logger {}

internal class DataRepository private constructor(
    private val observers: MutableList<DataObserver>,
) {
    constructor() : this(mutableListOf())

    companion object {
        private val objectMapper = ObjectMapper()

        @Language("PostgreSQL")
        val finnRaderTilSlettingQuery = """
            SELECT id, data FROM arena_data 
            WHERE data IS NOT NULL 
                AND behandlet IS NULL
            ORDER BY sletterekkefolge ASC LIMIT ?
        """.trimIndent()
    }

    fun addObserver(observer: DataObserver) = observers.add(observer)

    @Language("PostgreSQL")
    private val lagreQuery =
        """INSERT INTO arena_data (tabell, pos, skjedde, replikert, data)
        |VALUES (?, ?, ?, ?, ?::jsonb)
        |ON CONFLICT DO NOTHING
        |""".trimMargin()

    fun lagre(tabell: String, pos: String, skjedde: LocalDateTime, replikert: LocalDateTime, json: String) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource, returnGeneratedKey = true)) { session ->
            opprettRotObjekter(json)
            session.run(
                queryOf(
                    lagreQuery,
                    tabell,
                    pos,
                    skjedde,
                    replikert,
                    json
                ).asUpdateAndReturnGeneratedKey
            )
        }.also {
            observers.forEach { observer -> observer.nyData(NyDataEvent("Type", it, erDagpenger(json))) } //TODO: Legg til riktig type
        }

    internal fun batchSlettDataSomIkkeOmhandlerDagpenger(batchStørrelse: Int) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            val iderTilSletting = hentRaderSomSkalSlettes(session, batchStørrelse)
            slettRader(session, iderTilSletting)
        }

    @Language("PostgreSQL")
    private fun hentRaderSomSkalSlettes(session: Session, batchStørrelse: Int): List<List<Long>> {
        return session.run(
            queryOf(finnRaderTilSlettingQuery, batchStørrelse).map {
                if (erDagpenger(it.string("data")) == false) {
                    listOf(it.long("id"))
                } else {
                    utsettSletting(session, it.long("id"))
                    null
                }
            }.asList
        )
    }

    private fun utsettSletting(session: Session, primærnøkkel: Long?) {
        @Language("PostgreSQL")
        val updateQuery = """
            UPDATE arena_data
            SET sletterekkefolge=NEXTVAL('arena_data_sletterekkefolge_seq'),
                antall_slettevurderinger=antall_slettevurderinger + 1
            WHERE id=?
        """.trimIndent()
        session.run(queryOf(updateQuery, primærnøkkel).asExecute)
    }

    @Language("PostgreSQL")
    private fun slettRader(session: Session, iderTilSletting: List<List<Long>>) =
        session.batchPreparedStatement("UPDATE arena_data SET data=NULL, behandlet=NOW() WHERE id=?", iderTilSletting)

    @Language("PostgreSQL")
    internal fun slettRad(primærnøkkel: Long?) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf("UPDATE arena_data SET data=NULL, behandlet=NOW() WHERE id=?", primærnøkkel).asExecute)
        }

    private fun erDagpenger(data: String): Boolean? {
        val json = objectMapper.readTree(data)
        opprettRotObjekter(json)

        return when (json["table"].asText()) {
            "SIAMO.SAK" -> json["after"]["SAKSKODE"].asText() == "DAGP"
            "SIAMO.VEDTAK" -> erDpVedtak(json["after"]["VEDTAK_ID"].asInt())
            "SIAMO.VEDTAKFAKTA" -> erDpVedtak(json["after"]["VEDTAK_ID"].asInt())
            "SIAMO.BEREGNINGSLEDD" -> if (json["after"]["TABELLNAVNALIAS_KILDE"].asText() == "VEDTAK") erDpVedtak(json["after"]["OBJEKT_ID_KILDE"].asInt()) else null
            else -> null
        }
    }

    private fun opprettRotObjekter(data: String) = opprettRotObjekter(objectMapper.readTree(data))

    private fun opprettRotObjekter(json: JsonNode) {
        when (json["table"].asText()) {
            "SIAMO.SAK" -> lagreSak(json["after"]["SAK_ID"].asInt(), json["after"]["SAKSKODE"].asText())
            "SIAMO.VEDTAK" -> lagreVedtak(json["after"]["VEDTAK_ID"].asInt(), json["after"]["SAK_ID"].asInt())
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

    private fun lagreSak(sakId: Int, saksKode: String) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    "INSERT INTO sak (sak_id,er_dagpenger) VALUES(?,?) ON CONFLICT DO NOTHING",
                    sakId,
                    saksKode == "DAGP"
                ).asUpdate
            )
        }

    private fun lagreVedtak(vedtakId: Int, sakId: Int) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """INSERT INTO vedtak (vedtak_id, sak_id)
                        |VALUES (?, ?)
                        |ON CONFLICT DO UPDATE SET sist_oppdatert=NOW()
                        |""".trimMargin(), vedtakId, sakId
                ).asUpdate
            )
        }

    fun hentVedtaksdata(vedtakId: Int) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """SELECT data
                    |FROM arena_data
                    |WHERE data @> ?::jsonb
                    |   OR data @> ?::jsonb
                    |   OR data @> ?::jsonb""".trimMargin(),
                    """{ "table": "SIAMO.VEDTAK", "after": { "VEDTAK_ID": $vedtakId }}""",
                    """{ "table": "SIAMO.VEDTAKFAKTA", "after": { "VEDTAK_ID": $vedtakId }}""",
                    """{ "table": "SIAMO.BEREGNINGSLEDD", "after": { "TABELLNAVNALIAS_KILDE": "VEDTAK", "OBJEKT_ID_KILDE": $vedtakId }}"""
                ).map {
                    it.string("data")
                }.asList
            )
        }

    internal interface DataObserver {
        data class NyDataEvent(val type: String, val primærnøkkel: Long?, val erDagpenger: Boolean?)

        fun nyData(nyDataEvent: NyDataEvent)
    }

    internal class SlettUønsketYtelseObserver(private val dataRepository: DataRepository) : DataObserver {

        override fun nyData(nyDataEvent: NyDataEvent) {
            if (nyDataEvent.erDagpenger == false) {
                dataRepository.slettRad(nyDataEvent.primærnøkkel)
            }
        }
    }
    internal class OppdaterVedtakObserver(private val dataRepository: DataRepository) : DataObserver {
        override fun nyData(nyDataEvent: NyDataEvent) {
            if (nyDataEvent.erDagpenger == true) {
                //oppdaterVedtak(nyDataEvent)
            }
        }
    }
}
