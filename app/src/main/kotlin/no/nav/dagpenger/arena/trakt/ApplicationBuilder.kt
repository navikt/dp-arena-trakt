package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.clean
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
    ).build { _, _ ->
        clean()
    }

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration().also {
            val sakRepository = SakRepository()
            SakService(rapidsConnection, sakRepository)
            VedtakService(rapidsConnection, VedtakRepository(sakRepository))
        }
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {}
}
