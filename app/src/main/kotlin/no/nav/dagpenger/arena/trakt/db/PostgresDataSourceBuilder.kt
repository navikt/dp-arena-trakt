package no.nav.dagpenger.arena.trakt.db

import ch.qos.logback.core.util.OptionHelper
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.PropertyGroup
import com.natpryce.konfig.getValue
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.CleanResult
import org.flywaydb.core.internal.configuration.ConfigUtils
import javax.sql.DataSource

private val config = ConfigurationProperties.systemProperties() overriding EnvironmentVariables()

object PostgresDataSourceBuilder {
    object db : PropertyGroup() {
        val host by stringType
        val port by stringType
        val database by stringType
        val username by stringType
        val password by stringType
    }

    val dataSource: DataSource by lazy {
        HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", config[db.host])
            addDataSourceProperty("portNumber", config[db.port])
            addDataSourceProperty("databaseName", config[db.database])
            addDataSourceProperty("user", config[db.username])
            addDataSourceProperty("password", config[db.password])
            maximumPoolSize = 10
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        }
    }
    private fun getOrThrow(key: String): String = OptionHelper.getEnv(key) ?: OptionHelper.getSystemProperty(key)
    private val flywayBuilder = Flyway.configure().connectRetries(5)
    fun clean(): CleanResult {
        return flywayBuilder.cleanDisabled(
            getOrThrow(ConfigUtils.CLEAN_DISABLED).toBooleanStrict(),
        ).dataSource(dataSource).load().clean()
    }

    fun runMigration(initSql: String? = null) =
        Flyway.configure()
            .dataSource(dataSource)
            .initSql(initSql)
            .load()
            .migrate()
}
