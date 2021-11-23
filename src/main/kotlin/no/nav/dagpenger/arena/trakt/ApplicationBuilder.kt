package no.nav.dagpenger.arena.trakt

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.RapidsConnection.StatusListener

val log = KotlinLogging.logger {}

class ApplicationBuilder(config: Map<String, String>) : StatusListener {
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(config)
    ).build()

    init {
        rapidsConnection.register(this)
    }

    override fun onStartup(rapidsConnection: RapidsConnection) {
        log.info("starter dp-arena-trakt")
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()
}
