package testutil

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import org.testcontainers.containers.PostgreSQLContainer
import persistence.postgres.PostgresConfig

fun DI.MainBuilder.setupTestDependencies() {
    val postgresContainer = PostgreSQLContainer<Nothing>()
    bind<PostgresConfig>(overrides = true) with singleton {
        PostgresConfig(
            user = postgresContainer.username,
            password = postgresContainer.password,
            serverName = postgresContainer.host,
            port = postgresContainer.firstMappedPort,
            db = postgresContainer.databaseName
        )
    }
}
