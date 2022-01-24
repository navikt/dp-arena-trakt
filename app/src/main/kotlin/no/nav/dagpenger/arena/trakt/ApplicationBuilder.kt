package no.nav.dagpenger.arena.trakt

import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.Config.batchInsert
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.arena.trakt.tjenester.BeregningsleddService
import no.nav.dagpenger.arena.trakt.tjenester.DataMottakService
import no.nav.dagpenger.arena.trakt.tjenester.VedtakService
import no.nav.dagpenger.arena.trakt.tjenester.VedtaksfaktaService
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.RapidsConnection.StatusListener

val log = KotlinLogging.logger {}

internal class ApplicationBuilder(config: Map<String, String>) : StatusListener {
    // private lateinit var ferdigeHendelserPolling: Job
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(config)
    ).build { _, kafkaRapid ->
        // clean()
    }

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration().also {
            val hendelseRepository = HendelseRepository(rapidsConnection) // .also { ferdigeHendelserPolling = it.startAsync(30000L) }
            val repository = DataRepository(1000) // .also { it.addObserver(hendelseRepository) }

            if (batchInsert) {
                log.info { "** Starter dp-arena-trakt i batchmodus **" }
                DataMottakService(rapidsConnection, repository)
            } else {
                log.info { "** Starter dp-arena-trakt i vanlig modus **" }
                BeregningsleddService(rapidsConnection, hendelseRepository)
                VedtaksfaktaService(rapidsConnection, hendelseRepository)
                VedtakService(rapidsConnection, hendelseRepository)
            }
        }
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        // ferdigeHendelserPolling.cancel()
    }
}
