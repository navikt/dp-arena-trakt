package no.nav.dagpenger.arena.trakt

import io.ktor.server.engine.ApplicationEngine
import no.nav.helse.rapids_rivers.KtorBuilder

fun main() {
    val application: ApplicationEngine = KtorBuilder().liveness { true }.readiness { true }.port(8080).build()
    application.start()

    Runtime.getRuntime().addShutdownHook(
        Thread { application.stop(gracePeriodMillis = 100, timeoutMillis = 101L) }
    )
}
