package no.nav.dagpenger.arena.trakt

import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.db.ArenaMottakRepository
import no.nav.dagpenger.arena.trakt.tjenester.DataMottakService
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.RapidsConnection.StatusListener

val log = KotlinLogging.logger {}

internal class ApplicationBuilder(config: Map<String, String>) : StatusListener {
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(config)
    ).build { _, kafkaRapid ->
        log.info { "App starting with $config" }
        if (config["offset"] == "earliest") kafkaRapid.seekToBeginning()
        // clean()
    }

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()

    private val arenaMottakRepository = ArenaMottakRepository()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        DataMottakService(rapidsConnection, arenaMottakRepository)
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        arenaMottakRepository.stop()
    }
}
