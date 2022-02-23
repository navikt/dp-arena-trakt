package no.nav.dagpenger.arena.trakt

import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.arena.trakt.db.PubliserNyttVedtakObserver
import no.nav.dagpenger.arena.trakt.db.SakRepository
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
import no.nav.dagpenger.arena.trakt.tjenester.SakSink
import no.nav.dagpenger.arena.trakt.tjenester.VedtakSink
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.RapidsConnection.StatusListener
import java.util.Timer

val logger = KotlinLogging.logger { }

internal class ApplicationBuilder(config: Map<String, String>) : StatusListener {
    private lateinit var periodiskSjekk: Timer
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
            val sakRepository = SakRepository()
            val hendelseRepository = HendelseRepository(rapidsConnection)
            val vedtakRepository = VedtakRepository(sakRepository).also {
                it.leggTilObserver(PubliserNyttVedtakObserver(hendelseRepository))
            }
            SakSink(rapidsConnection, sakRepository)
            VedtakSink(rapidsConnection, vedtakRepository)
            // periodiskSjekk = PeriodiskUsendtVedtakSjekk(sakRepository, vedtakRepository).start()
        }
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        if (::periodiskSjekk.isInitialized) {
            periodiskSjekk.cancel()
        }
    }
}
