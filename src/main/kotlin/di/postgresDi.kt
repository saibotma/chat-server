package di

import io.ktor.config.*
import org.kodein.di.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.ChatServerPostgres
import persistence.postgres.PostgresConfig
import util.*

val postgresDi = DI.Module("postgres") {
    bind<PostgresConfig>() with singleton {
        val hocon: HoconApplicationConfig = instance()
        PostgresConfig(
            user = hocon.postgresUser,
            password = hocon.postgresPassword,
            serverName = hocon.postgresServerName,
            port = hocon.postgresPort,
            db = hocon.postgresDb
        )
    }
    bind<ChatServerPostgres>() with singleton { ChatServerPostgres(instance(), instance()) }
    bind<KotlinDslContext>() with provider { instance<ChatServerPostgres>().kotlinDslContext }
}
