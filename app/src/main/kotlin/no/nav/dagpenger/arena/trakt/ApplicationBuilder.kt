package no.nav.dagpenger.arena.trakt

import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.DataRepository.OppdaterVedtakObserver
import no.nav.dagpenger.arena.trakt.db.DataRepository.SlettUønsketYtelseObserver
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.arena.trakt.db.Sletterutine
import no.nav.dagpenger.arena.trakt.tjenester.DataMottakService
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.RapidsConnection.StatusListener
import java.util.Timer

val log = KotlinLogging.logger {}

internal class ApplicationBuilder(config: Map<String, String>) : StatusListener {
    private lateinit var sletterutine: Timer
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(config)
    ).build()

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration().also {
            val hendelseRepository =
                HendelseRepository(rapidsConnection) // .also { ferdigeHendelserPolling = it.startAsync(30000L) }
            val repository = DataRepository().apply {
                addObserver(SlettUønsketYtelseObserver(this))
                addObserver(OppdaterVedtakObserver(this))
            }
            sletterutine = Sletterutine(repository).start()
            DataMottakService(rapidsConnection, repository)
            // BeregningsleddService(rapidsConnection, hendelseRepository)
            // VedtaksfaktaService(rapidsConnection, hendelseRepository)
            // VedtakService(rapidsConnection, hendelseRepository)
        }
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        sletterutine.cancel()
    }
}
