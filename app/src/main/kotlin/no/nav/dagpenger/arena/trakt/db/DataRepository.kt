package no.nav.dagpenger.arena.trakt.db

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.BEREGNINGSLEDD_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.DAGPENGE_SAK
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.SAK_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.VEDTAKFAKTA_TABELL
import no.nav.dagpenger.arena.trakt.db.ArenaKoder.VEDTAK_TABELL
import no.nav.dagpenger.arena.trakt.db.DataRepository.DataObserver.NyDataEvent
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

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

    fun lagre(tabell: String, pos: String, skjedde: LocalDateTime, replikert: LocalDateTime, json: String): Long? {
        val arenaRad = ArenaRad.lagRad(tabell, json)
        return using(sessionOf(PostgresDataSourceBuilder.dataSource, returnGeneratedKey = true)) { session ->
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
            val nyDataEvent = NyDataEvent(arenaRad, it, erDagpenger(json))
            observers.forEach { observer ->
                observer.nyData(nyDataEvent)
            }
        }
    }

    internal fun batchSlettDataSomIkkeOmhandlerDagpenger(batchStørrelse: Int) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            val iderTilSletting = hentRaderSomSkalSlettes(session, batchStørrelse)
            logger.info { "Fant ${iderTilSletting.size} rader som skal slettes" }
            slettRader(session, iderTilSletting)
        }

    @Language("PostgreSQL")
    private fun hentRaderSomSkalSlettes(session: Session, batchStørrelse: Int): List<List<Long>> {
        val ids = mutableListOf<List<Long>>()
        val query = queryOf(finnRaderTilSlettingQuery, batchStørrelse)
        logger.info { "Kjører query: $query" }
        session.forEach(query) {
            val data = it.string("data")
            opprettRotObjekter(data)

            if (erDagpenger(data) == false) {
                ids.add(listOf(it.long("id")))
                logger.info { "Rad med id(${it.long("id")}) skal slettes" }
            } else {
                utsettSletting(session, it.long("id"))
                logger.info { "Rad med id(${it.long("id")}) skal ikke slettes (nå)" }
                // TODO: Vi bør kunne markere rader som aldri skal slettes
            }
        }

        return ids.toList()
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

        return when (json["table"].asText()) {
            SAK_TABELL -> json["after"]["SAKSKODE"].asText() == DAGPENGE_SAK
            VEDTAK_TABELL -> erDpVedtak(json["after"]["VEDTAK_ID"].asInt())
            VEDTAKFAKTA_TABELL -> erDpVedtak(json["after"]["VEDTAK_ID"].asInt())
            BEREGNINGSLEDD_TABELL -> tilhørerBeregningsleddDpVedtak(json)
            else -> null
        }
    }

    private fun tilhørerBeregningsleddDpVedtak(json: JsonNode) =
        if (json["after"]["TABELLNAVNALIAS_KILDE"].asText() == "VEDTAK") {
            erDpVedtak(json["after"]["OBJEKT_ID_KILDE"].asInt())
        } else null

    private fun opprettRotObjekter(data: String) = opprettRotObjekter(objectMapper.readTree(data))

    private fun opprettRotObjekter(json: JsonNode) {
        when (json["table"].asText()) {
            SAK_TABELL -> lagreSak(json["after"]["SAK_ID"].asInt(), json["after"]["SAKSKODE"].asText())
            VEDTAK_TABELL -> lagreVedtak(json["after"]["VEDTAK_ID"].asInt(), json["after"]["SAK_ID"].asInt())
        }
    }

    //language=PostgreSQL
    private fun erDpVedtak(vedtakId: Int): Boolean? {
        val kanVedtakKnyttesTilSakQuery = """
            SELECT er_dagpenger FROM sak
                LEFT JOIN vedtak ON sak.sak_id = vedtak.sak_id
            WHERE vedtak.vedtak_id = ?
        """.trimIndent()

        return using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(kanVedtakKnyttesTilSakQuery, vedtakId).map {
                    it.boolean("er_dagpenger")
                }.asSingle
            )
        }
    }

    private fun lagreSak(sakId: Int, saksKode: String) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                //language=PostgreSQL
                queryOf(
                    """INSERT INTO sak (sak_id,er_dagpenger) VALUES(?,?) ON CONFLICT DO NOTHING""",
                    sakId,
                    saksKode == DAGPENGE_SAK
                ).asUpdate
            )
        }

    private fun lagreVedtak(vedtakId: Int, sakId: Int) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                //language=PostgreSQL
                queryOf(
                    """
                    |INSERT INTO vedtak (vedtak_id, sak_id)
                    |VALUES (?, ?)
                    |ON CONFLICT (vedtak_id) DO UPDATE 
                    |    SET sist_oppdatert=NOW(), antall_oppdateringer = vedtak.antall_oppdateringer + 1
                """.trimMargin(),
                    vedtakId, sakId
                ).asUpdate
            )
        }

    private fun oppdaterVedtak(vedtakId: Int): Int {
        return using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                //language=PostgreSQL
                queryOf(
                    """
                        UPDATE vedtak
                        SET sist_oppdatert=NOW(),
                            antall_oppdateringer = antall_oppdateringer + 1
                        WHERE vedtak_id = ? 
                        """.trimMargin(),
                    vedtakId
                ).asUpdate
            )
        }
    }

    fun hentVedtaksdata(vedtakId: Int): List<String> {
        return using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                //language=PostgreSQL
                queryOf(
                    """
                    |SELECT data
                    |FROM arena_data
                    |WHERE data @> ?::jsonb
                    |   OR data @> ?::jsonb
                    |   OR data @> ?::jsonb""".trimMargin(),
                    """{ "table": "$VEDTAK_TABELL", "after": { "VEDTAK_ID": $vedtakId }}""",
                    """{ "table": "$VEDTAKFAKTA_TABELL", "after": { "VEDTAK_ID": $vedtakId }}""",
                    """{ "table": "$BEREGNINGSLEDD_TABELL", "after": { "TABELLNAVNALIAS_KILDE": "VEDTAK", "OBJEKT_ID_KILDE": $vedtakId }}"""
                ).map {
                    it.string("data")
                }.asList
            )
        }
    }

    internal interface DataObserver {
        data class NyDataEvent(val arenaRad: ArenaRad, val primærnøkkel: Long?, val erDagpenger: Boolean?)

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
            if (nyDataEvent.erDagpenger == true && nyDataEvent.arenaRad.vedtakId() != null) {
                dataRepository.oppdaterVedtak(nyDataEvent.arenaRad.vedtakId()!!)
            }
        }
    }
}
