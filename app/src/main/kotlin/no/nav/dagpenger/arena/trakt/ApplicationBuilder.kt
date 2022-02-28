package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.arena.trakt.db.Replikeringslogg
import no.nav.dagpenger.arena.trakt.db.SakRepository
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
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

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration().also {
            val sakRepository = SakRepository()
            val vedtakRepository = VedtakRepository(sakRepository)
            val hendelseRepository = HendelseRepository(rapidsConnection)
            ReplikeringMediator(
                rapidsConnection,
                RadMottak(sakRepository, vedtakRepository, hendelseRepository),
                Replikeringslogg()
            )
        }
    }
}
