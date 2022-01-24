import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.PropertyGroup
import com.natpryce.konfig.getValue
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import com.zaxxer.hikari.HikariDataSource

private val config = ConfigurationProperties.systemProperties() overriding EnvironmentVariables()

internal object PostgresDataSourceBuilder {
    internal object db : PropertyGroup() {
        val host by stringType
        val port by stringType
        val database by stringType
        val username by stringType
        val password by stringType
    }

    val dataSource by lazy {
        HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", config[db.host])
            addDataSourceProperty("portNumber", config[db.port])
            addDataSourceProperty("databaseName", config[db.database])
            addDataSourceProperty("user", config[db.username])
            addDataSourceProperty("password", config[db.password])
            maximumPoolSize = 20
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        }
    }
}
