import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.time.Period

internal class DataRepository private constructor(
    private val batchSize: Int,
    private val observers: MutableList<DataObserver>
) {
    constructor() : this(1, mutableListOf())
    constructor(batchSize: Int) : this(batchSize, mutableListOf())

    private val params: MutableList<List<Any>> = mutableListOf()
    fun addObserver(observer: DataObserver) = observers.add(observer)

    @Language("PostgreSQL")
    private val lagreQuery =
        """INSERT INTO arena_data (tabell, pos, skjedde, replikert, data)
        |VALUES (?, ?, ?, ?, ?::jsonb)
        |ON CONFLICT DO NOTHING""".trimMargin()

    fun lagre(tabell: String, pos: String, skjedde: LocalDateTime, replikert: LocalDateTime, json: String) {
        params.add(listOf(tabell, pos, skjedde, replikert, json))

        if (params.size >= batchSize) {
            using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
                session.batchPreparedStatement(lagreQuery, params)
                params.clear()
                /*session.run(
                    queryOf(
                        lagreQuery,
                        tabell,
                        pos,
                        skjedde,
                        replikert,
                        json
                    ).asUpdate*/
                // )
            }.also {
                observers.forEach { it.nyData() }
            }
        }
    }

    @Language("PostgreSQL")
    private val slettQuery =
        """DELETE FROM arena_data WHERE mottatt < CURRENT_TIMESTAMP - INTERVAL '1 days' * ? AND hendelse_id IS NULL """

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
