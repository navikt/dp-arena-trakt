package no.nav.dagpenger.arena.trakt.db

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.PropertyGroup
import com.natpryce.konfig.getValue
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway

private val config = ConfigurationProperties.systemProperties() overriding EnvironmentVariables()

internal object PostgresDataSourceBuilder {
    internal object dbt : PropertyGroup() {
        val host by stringType
        val port by stringType
        val database by stringType
        val username by stringType
        val password by stringType
    }

    val dataSource by lazy {
        HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", config[dbt.host])
            addDataSourceProperty("portNumber", config[dbt.port])
            addDataSourceProperty("databaseName", config[dbt.database])
            addDataSourceProperty("user", config[dbt.username])
            addDataSourceProperty("password", config[dbt.password])
            maximumPoolSize = 10
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        }
    }

    fun clean() = Flyway.configure().connectRetries(5).dataSource(dataSource).load().clean()

    internal fun runMigration(initSql: String? = null) =
        Flyway.configure()
            .dataSource(dataSource)
            .initSql(initSql)
            .load()
            .migrate()
}
