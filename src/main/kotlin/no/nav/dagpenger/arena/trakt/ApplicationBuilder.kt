package no.nav.dagpenger.arena.trakt

import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.clean
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.arena.trakt.tjenester.BeregningsleddService
import no.nav.dagpenger.arena.trakt.tjenester.DataMottakService
import no.nav.dagpenger.arena.trakt.tjenester.VedtakHendelseService
import no.nav.dagpenger.arena.trakt.tjenester.VedtaksfaktaService
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.RapidsConnection.StatusListener

val log = KotlinLogging.logger {}

internal class ApplicationBuilder(config: Map<String, String>) : StatusListener {
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(config)
    ).build { _, kafkaRapid -> kafkaRapid.seekToBeginning() }

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        clean()
        runMigration().also {
            val repository = DataRepository()
            val hendelseRepository = HendelseRepository(rapidsConnection)

            DataMottakService(rapidsConnection, repository, hendelseRepository)
            BeregningsleddService(rapidsConnection, hendelseRepository)
            VedtaksfaktaService(rapidsConnection, hendelseRepository)
            VedtakHendelseService(rapidsConnection, hendelseRepository)
        }
    }
}
