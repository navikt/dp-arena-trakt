package no.nav.dagpenger.arena.trakt

import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.overriding

internal object Config {
    private fun lagArenaTopicNavn(navn: String, miljø: String) = "teamarenanais.aapen-arena-${navn}endret-v1-$miljø"
    private fun arenaTopics(miljø: String) = listOf(
        "beregningsledd",
        "vedtakfakta",
        "vedtak",
        // "kvotebruk",
        // "beregningslogg",
        // "meldekort",
        // "sak"
    ).joinToString(",") { lagArenaTopicNavn(it, miljø) }

    private val defaultProperties = ConfigurationMap(
        mapOf(
            "DB_DATABASE" to "arena-data",
            "DB_HOST" to "localhost",
            "DB_PASSWORD" to "password",
            "DB_PORT" to "5432",
            "DB_USERNAME" to "username",
            "HTTP_PORT" to "8080",
            "RAPID_APP_NAME" to "dp-arena-trakt",
            "KAFKA_CONSUMER_GROUP_ID" to "dp-arena-trakt-v7",
            "KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1",
            "KAFKA_EXTRA_TOPIC" to arenaTopics(miljø = "q2"),
            "KAFKA_RESET_POLICY" to "earliest",
            "BATCH_INSERT" to "false"
        )
    )
    private val prodProperties = ConfigurationMap(
        mapOf()
    )
    private val properties: Configuration by lazy {
        val systemAndEnvProperties = ConfigurationProperties.systemProperties() overriding EnvironmentVariables()
        when (System.getenv().getOrDefault("NAIS_CLUSTER_NAME", "LOCAL")) {
            "prod-gcp" -> systemAndEnvProperties overriding prodProperties overriding defaultProperties
            else -> systemAndEnvProperties overriding defaultProperties
        }
    }
    val batchInsert = properties[Key("BATCH_INSERT", booleanType)]

    val config: Map<String, String> = properties.list().reversed().fold(emptyMap()) { map, pair ->
        map + pair.second
    }
}
