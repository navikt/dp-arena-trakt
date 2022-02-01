package no.nav.dagpenger.arena.trakt.db

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.PropertyGroup
import com.natpryce.konfig.getValue
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

private val config = ConfigurationProperties.systemProperties() overriding EnvironmentVariables()

object GcpPostgresDataSourceBuilder {
    object db : PropertyGroup() {
        object job : PropertyGroup() {
            val database by stringType
            val username by stringType
            val password by stringType
            val instance by stringType
        }
    }

    val dataSource: DataSource by lazy {
        HikariDataSource().apply {
            jdbcUrl = String.format("jdbc:postgresql:///%s", config[db.job.database])
            addDataSourceProperty("user", config[db.job.username])
            addDataSourceProperty("password", config[db.job.password])
            addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory")
            addDataSourceProperty("cloudSqlInstance", config[db.job.instance])
            maximumPoolSize = 20
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        }
    }

    fun clean() = Flyway.configure().connectRetries(5).dataSource(PostgresDataSourceBuilder.dataSource).load().clean()

}
