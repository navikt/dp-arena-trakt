package no.nav.dagpenger.arena.trakt.db

import com.zaxxer.hikari.HikariDataSource
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.log
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal class ArenaMottakRepository constructor(
    private val batchSize: Int,
) {
    constructor() : this(200)

    private val shutdown = CountDownLatch(1)
    private val running = AtomicBoolean(true)
    private val rows = Collections.synchronizedList(mutableListOf<List<Any>>())
    private val dataSource by lazy {
        (PostgresDataSourceBuilder.dataSource as HikariDataSource).apply {
            addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory")
            addDataSourceProperty("cloudSqlInstance", "dp-arena-trakt-v1")
        }
    }

    fun leggTil(tabell: String, pos: String, skjedde: LocalDateTime, replikert: LocalDateTime, json: String) {
        if (!running.get()) throw IllegalStateException("Shutting down, not accepting new writes")

        rows.add(listOf(tabell, pos, skjedde, replikert, json))

        if (rows.size >= batchSize) lagre()
    }

    private fun lagre() {
        using(sessionOf(dataSource)) { session ->
            session.batchPreparedStatement(lagreQuery, rows).also {
                if (!running.get()) shutdown.countDown()
            }
            rows.clear()
        }
    }

    @Language("PostgreSQL")
    private val lagreQuery =
        """INSERT INTO arena_data (tabell, pos, skjedde, replikert, data)
        |VALUES (?, ?, ?, ?, ?::jsonb)
        |ON CONFLICT DO NOTHING""".trimMargin()

    fun stop() {
        if (!running.getAndSet(false)) return log.error("Already in process of shutting down")
        log.info("Received shutdown signal. Waiting 10 seconds for finishing batch in flight")
        if (rows.size != 0) lagre()
        shutdown.await(10, TimeUnit.SECONDS)
    }
}
