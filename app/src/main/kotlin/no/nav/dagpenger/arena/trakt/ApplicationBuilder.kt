package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.arena.trakt.db.SakRepository
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
import no.nav.dagpenger.arena.trakt.tjenester.SakService
import no.nav.dagpenger.arena.trakt.tjenester.VedtakService
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.RapidsConnection.StatusListener

internal class ApplicationBuilder(config: Map<String, String>) : StatusListener {
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
            SakService(rapidsConnection, SakRepository())
            VedtakService(rapidsConnection, VedtakRepository())
        }
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {}
}
