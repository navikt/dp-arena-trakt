package no.nav.dagpenger.arena.trakt.db

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val logg = KotlinLogging.logger {}

internal class DataRepository private constructor(
    private val observers: MutableList<DataObserver>,
) {
    constructor() : this(mutableListOf())

    companion object {
        private val objectMapper = ObjectMapper()
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
            when (erDagpenger(json)) {
                false -> slettRad(it)
                true -> observers.forEach { observer -> observer.nyData() }
                // null -> TODO("Kan ikke avgjøre om dette er dagpenger, må vente på mer data")
            }
        }

    internal fun batchSlettDataSomIkkeOmhandlerDagpenger(batchStørrelse: Int) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            val iderTilSletting = hentRaderSomSkalSlettes(session, batchStørrelse)
            println("Slettes: $iderTilSletting")
            slettRader(session, iderTilSletting)
        }

    @Language("PostgreSQL")
    private fun hentRaderSomSkalSlettes(session: Session, batchStørrelse: Int) = session.run(
        queryOf(
            """
            SELECT id, data FROM arena_data 
            WHERE data IS NOT NULL 
                AND behandlet IS NULL
                AND vurderes_slettet < now()
            ORDER BY id ASC LIMIT ?
            """.trimIndent(),
            batchStørrelse
        ).map {
            if (erDagpenger(it.string("data")) == false) {
                listOf(it.long("id"))
            } else {
                utsettSletting(session, it.long("id"))
                null
            }
        }.asList
    )

    @Language("PostgreSQL")
    private fun utsettSletting(session: Session, primærnøkkel: Long?): Boolean {
        logg.info { "Kan ikke bestemme ytelse for data med Id: $primærnøkkel nå. Prøver behandling senere" }
        return session.run(queryOf("UPDATE arena_data SET vurderes_slettet=(now() + INTERVAL '5 minutes') WHERE id=?", primærnøkkel).asExecute)
    }

    @Language("PostgreSQL")
    private fun slettRader(session: Session, iderTilSletting: List<List<Long>>) =
        session.batchPreparedStatement("UPDATE arena_data SET data=NULL, behandlet=NOW() WHERE id=?", iderTilSletting)

    @Language("PostgreSQL")
    private fun slettRad(primærnøkkel: Long?) =
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
                    "INSERT INTO vedtak (vedtak_id,sak_id) VALUES(?,?) ON CONFLICT DO NOTHING", vedtakId, sakId
                ).asUpdate
            )
        }

    internal interface DataObserver {
        fun nyData() {}
    }
}
