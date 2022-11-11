package di

import flyway.FlywayConfig
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.Configuration
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.kodein.di.*
import org.postgresql.ds.PGSimpleDataSource
import persistence.jooq.JacksonKotlinConverterProvider
import persistence.jooq.KotlinDslContext
import persistence.postgres.PostgresConfig
import util.*
import javax.sql.DataSource

val postgresDi = DI.Module("postgres") {
    bind<FlywayConfig>() with singleton {
        val hocon: ApplicationConfig by di.instance()
        FlywayConfig(baselineVersion = hocon.flywayBaselineVersion, shouldBaseline = hocon.flywayShouldBaseline)
    }

    bind<PostgresConfig>() with singleton {
        val hocon: ApplicationConfig by di.instance()
        PostgresConfig(
            user = hocon.postgresUser,
            password = hocon.postgresPassword,
            serverName = hocon.postgresServerName,
            port = hocon.postgresPort,
            db = hocon.postgresDb
        )
    }
    bind<DataSource>() with singleton {
        val config: PostgresConfig by di.instance()
        PGSimpleDataSource().apply {
            databaseName = config.db
            user = config.user
            password = config.password
            serverNames = arrayOf(config.serverName)
            portNumbers = intArrayOf(config.port)
            // Enable batch insert with just one insert statement.
            // https://github.com/JetBrains/Exposed/wiki/DSL#batch-insert
            // TODO: Research how this works with JOOQ
            reWriteBatchedInserts = true
        }
    }
    bind<Flyway>() with singleton {
        val configuration = buildFlywayConfiguration(dataSource = instance(), isCleanDisabled = true)
        Flyway(configuration)
    }
    bind<DSLContext>() with singleton {
        val dataSource: DataSource by di.instance()
        val converterProvider: JacksonKotlinConverterProvider by di.instance()
        DSL.using(dataSource, SQLDialect.POSTGRES).apply {
            configuration().set(converterProvider)
        }
    }
    bind<KotlinDslContext>() with singleton { KotlinDslContext(instance()) }
}

// ⚠️ Configuration here should be the same as in build.gradle.kts
fun buildFlywayConfiguration(dataSource: DataSource, isCleanDisabled: Boolean): Configuration {
    return FluentConfiguration()
        .baselineVersion("1")
        .baselineOnMigrate(true)
        .dataSource(dataSource)
        .cleanDisabled(isCleanDisabled)
}

