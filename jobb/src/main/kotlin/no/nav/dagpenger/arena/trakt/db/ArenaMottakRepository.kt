package no.nav.dagpenger.arena.trakt.db

import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.log
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.sql.DataSource

private val logg = KotlinLogging.logger {}
private val sikkerLogg = KotlinLogging.logger("tjenestekall.ArenaMottakRepository")

internal class ArenaMottakRepository private constructor(
    private val batchSize: Int,
    private val dataSource: DataSource
) {
    constructor(batchSize: Int) : this(batchSize, PostgresDataSourceBuilder.dataSource)
    constructor(dataSource: DataSource) : this(200, dataSource)

    private val shutdown = CountDownLatch(1)
    private val running = AtomicBoolean(true)
    private val rows = Collections.synchronizedList(mutableListOf<List<Any>>())

    fun leggTil(tabell: String, pos: String, skjedde: LocalDateTime, replikert: LocalDateTime, json: String) {

        try {
            if (!running.get()) throw IllegalStateException("Shutting down, not accepting new writes")

            rows.add(listOf(tabell, pos, skjedde, replikert, json))

            if (rows.size >= batchSize) lagre()
        } catch (e: Exception) {
            log.error { "Feil under innlasting av data. Se sikkerlogg for mer info." }
            sikkerLogg.error(e) { "Feil under innlasting av data." }
        }
    }

    private fun lagre() {
        using(sessionOf(dataSource)) { session ->
            session.batchPreparedStatement(lagreQuery, rows).also { rader ->
                logg.info { "Lagret batch med batchSize=$batchSize, lagret=${rader.filter { it == 1 }.size}, shuttingDown=${!running.get()}" }
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
