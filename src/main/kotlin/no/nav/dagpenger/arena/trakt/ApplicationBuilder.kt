package no.nav.dagpenger.arena.trakt

import mu.KotlinLogging
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
import java.util.Timer

val log = KotlinLogging.logger {}

internal class ApplicationBuilder(config: Map<String, String>) : StatusListener {
    private lateinit var hendelseTimer: Timer
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(config)
    ).build() /*{ _, kafkaRapid ->
        kafkaRapid.seekToBeginning()
        clean()
    }*/

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop().also { hendelseTimer.cancel() }

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration().also {
            val hendelseRepository = HendelseRepository(rapidsConnection).also {
                hendelseTimer = it.start()
            }
            val repository = DataRepository().also { it.addObserver(hendelseRepository) }

            DataMottakService(rapidsConnection, repository)
            BeregningsleddService(rapidsConnection, hendelseRepository)
            VedtaksfaktaService(rapidsConnection, hendelseRepository)
            VedtakService(rapidsConnection, hendelseRepository)
        }
    }
}
