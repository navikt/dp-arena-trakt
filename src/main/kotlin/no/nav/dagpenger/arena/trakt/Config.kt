package no.nav.dagpenger.arena.trakt

import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import com.zaxxer.hikari.HikariDataSource

internal object Config {
    private fun arenaGoldenGateTopics(miljø: String) = """
        teamarenanais.aapen-arena-beregningsleddendret-v1-$miljø,
        teamarenanais.aapen-arena-vedtakfaktaendret-v1-$miljø,
        teamarenanais.aapen-arena-vedtak-v1-$miljø,
        teamarenanais.aapen-arena-kvotebruk-v1-$miljø,
        teamarenanais.aapen-arena-beregningslogg-v1-$miljø,
        teamarenanais.aapen-arena-meldekort-v1-$miljø,
        teamarenanais.aapen-arena-sak-v1-$miljø,
        """.trimMargin()

    private val defaultProperties = ConfigurationMap(
        mapOf(
            "DB_DATABASE" to "arena-data",
            "DB_HOST" to "localhost",
            "DB_PASSWORD" to "password",
            "DB_PORT" to "5432",
            "DB_USERNAME" to "username",
            "HTTP_PORT" to "8080",
            "RAPID_APP_NAME" to "dp-arena-trakt",
            "KAFKA_CONSUMER_GROUP_ID" to "dp-arena-trakt-v1",
            "KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1",
            "KAFKA_EXTRA_TOPIC" to arenaGoldenGateTopics(miljø = "q2"),
            "KAFKA_RESET_POLICY" to "latest",
        )
    )

    private val prodProperties = ConfigurationMap(
        mapOf()
    )

    val properties: Configuration by lazy {
        val systemAndEnvProperties = ConfigurationProperties.systemProperties() overriding EnvironmentVariables()
        when (System.getenv().getOrDefault("NAIS_CLUSTER_NAME", "LOCAL")) {
            "prod-gcp" -> systemAndEnvProperties overriding prodProperties overriding defaultProperties
            else -> systemAndEnvProperties overriding defaultProperties
        }
    }

    val port = properties[Key("HTTP_PORT", intType)]

    val dataSource by lazy {
        HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", properties[Key("DB_HOST", stringType)])
            addDataSourceProperty("portNumber", properties[Key("DB_PORT", intType)])
            addDataSourceProperty("databaseName", properties[Key("DB_DATABASE", stringType)])
            addDataSourceProperty("user", properties[Key("DB_USERNAME", stringType)])
            addDataSourceProperty("password", properties[Key("DB_PASSWORD", stringType)])
            maximumPoolSize = 10
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        }
    }

    val config: Map<String, String> = properties.list().reversed().fold(emptyMap()) { map, pair ->
        map + pair.second
    }
}
