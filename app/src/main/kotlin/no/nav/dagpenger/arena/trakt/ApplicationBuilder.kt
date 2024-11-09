package no.nav.dagpenger.arena.trakt

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.arena.trakt.db.Replikeringslogg
import no.nav.dagpenger.arena.trakt.db.SakRepository
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
import no.nav.helse.rapids_rivers.RapidApplication

internal class ApplicationBuilder(
    config: Map<String, String>,
) : RapidsConnection.StatusListener {
    private val rapidsConnection = RapidApplication.create(config)

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
                Replikeringslogg(),
            )
        }
    }
}
