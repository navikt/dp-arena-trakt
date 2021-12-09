package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import org.intellij.lang.annotations.Language
import java.time.Period

private val logg = KotlinLogging.logger {}

internal class DataRepository private constructor(
    private val observers: MutableList<DataObserver>,
    private val pendingInserts: MutableList<List<String>>,
    private val batchSize: Int
) {
    internal constructor() : this(1)
    constructor(batchSize: Int) : this(mutableListOf(), mutableListOf(), batchSize)

    init {
        require(batchSize in 1..1000) { "Batch size må være mellom 1 og 1000 inserts" }
    }

    fun addObserver(observer: DataObserver) = observers.add(observer)

    @Language("PostgreSQL")
    private val lagreQuery = """INSERT INTO arena_data (data) VALUES(?::jsonb)"""

    fun lagre(json: String) = pendingInserts.add(listOf(json)).also {
        if (pendingInserts.size >= batchSize) batchLagre()
    }

    fun flush() = batchLagre()

    private fun batchLagre() {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.batchPreparedStatement(lagreQuery, pendingInserts)
        }.also {
            pendingInserts.clear()
            logg.info { "Lagret ${it.sum()} rader i batch}" }
            observers.forEach { it.nyData() }
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
