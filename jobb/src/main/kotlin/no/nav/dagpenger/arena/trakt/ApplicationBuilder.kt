package no.nav.dagpenger.arena.trakt

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.db.ArenaMottakRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.arena.trakt.tjenester.DataMottakService
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.RapidsConnection.StatusListener

val log = KotlinLogging.logger {}

internal class ApplicationBuilder(config: Map<String, String>) : StatusListener {
    private val dataSource by lazy {
        (PostgresDataSourceBuilder.dataSource as HikariDataSource).apply {
            addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory")
            addDataSourceProperty("cloudSqlInstance", "dp-arena-trakt-v1")
        }
    }
    private val arenaMottakRepository = ArenaMottakRepository(dataSource)
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(config)
    ).build { _, kafkaRapid ->
        if (config["offset"] == "earliest") kafkaRapid.seekToBeginning()
        // clean()
    }

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        DataMottakService(rapidsConnection, arenaMottakRepository)
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        arenaMottakRepository.stop()
    }
}
