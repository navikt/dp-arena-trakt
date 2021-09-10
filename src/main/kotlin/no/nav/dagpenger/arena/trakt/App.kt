package no.nav.dagpenger.arena.trakt

import io.ktor.server.engine.ApplicationEngine
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.helse.rapids_rivers.KtorBuilder

fun main() {
    val application: ApplicationEngine = KtorBuilder().liveness { true }.readiness { isDbReady() }.port(Config.port).build()
    application.start()

    Runtime.getRuntime().addShutdownHook(
        Thread { application.stop(gracePeriodMillis = 100, timeoutMillis = 101L) }
    )
}

fun isDbReady(): Boolean = using(
    sessionOf(dataSource = Config.dataSource)
) { session -> session.run(queryOf("SELECT 1").map { true }.asSingle) }!!
