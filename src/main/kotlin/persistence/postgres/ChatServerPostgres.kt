package persistence.postgres

import app.appella.persistence.jooq.JacksonKotlinConverterProvider
import persistence.jooq.KotlinDslContext
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.postgresql.ds.PGSimpleDataSource

class ChatServerPostgres(private val config: PostgresConfig, converterProvider: JacksonKotlinConverterProvider) {
    private val dataSource = PGSimpleDataSource().apply {
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

    // ⚠️ Configuration here must be the same as in build.gradle.kts
    private val flyway = Flyway.configure()
        .baselineVersion("1")
        .baselineOnMigrate(true)
        .dataSource(dataSource)
        .load()

    val dslContext = DSL.using(dataSource, SQLDialect.POSTGRES).apply {
        configuration().set(converterProvider)
    }

    val kotlinDslContext = KotlinDslContext(dslContext)

    fun runMigration(): MigrateResult = flyway.migrate()

    /**
     * ⚠️️⚠️️⚠️️⚠️️
     * Be careful !
     * Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.
     * ⚠️⚠️️⚠️️⚠️️
     * */
    fun clean() = flyway.clean()
}
