package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.intellij.lang.annotations.Language
import java.time.Period

internal class DataRepository private constructor(
    private val observers: MutableList<DataObserver>,
    private val blobs: MutableList<String>,
    private val bolkSize: Int
) {
    constructor() : this(mutableListOf(), mutableListOf(), 1)
    constructor(bolkSize: Int) : this(mutableListOf(), mutableListOf(), bolkSize)

    fun addObserver(observer: DataObserver) = observers.add(observer)

    @Language("PostgreSQL")
    private val lagreQuery = """INSERT INTO arena_data (data) VALUES(?::jsonb)"""

    fun lagre(json: String) {
        blobs.add(json)

        if (blobs.size >= bolkSize) {
            using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
                session.transaction { tx ->
                    tx.run(queryOf(lagreQuery, json).asUpdate)
                }
            }.also {
                observers.forEach { it.nyData() }
            }
        }
    }

    @Language("PostgreSQL")
    private val slettQuery =
        """DELETE FROM arena_data WHERE opprettet < CURRENT_TIMESTAMP - INTERVAL '1 days' * ? AND hendelse_id IS NULL """

    fun slettUbrukteData(eldreEnn: Period) {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    slettQuery,
                    eldreEnn.run {
                        // Legg til en dag så det blir *eldreEnn* og ikke eldreEnnOgIdag
                        plusDays(1)
                    }.days
                ).asUpdate
            )
        }
    }

    internal interface DataObserver {
        fun nyData() {}
    }
}
