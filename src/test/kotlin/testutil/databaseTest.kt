package testutil

import dev.saibotma.persistence.postgres.jooq.tables.Channel.Companion.CHANNEL
import dev.saibotma.persistence.postgres.jooq.tables.pojos.Channel
import di.setupKodein
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import persistence.postgres.ChatServerPostgres

fun databaseTest(
    bindDependencies: DI.MainBuilder.() -> Unit = {},
    test: suspend DatabaseTestEnvironment.() -> Unit
) {
    val kodein = DI {
        setupKodein()
        setupTestDependencies()
        bindDependencies()
    }
    val environment = DatabaseTestEnvironment(kodein)
    environment.resetDatabase()
    runBlocking { test(environment) }
}


open class DatabaseTestEnvironment(private val di: DI) {
    val postgres: ChatServerPostgres by di.instance()
    val database = postgres.kotlinDslContext
    fun resetDatabase() = di.direct.instance<ChatServerPostgres>().apply { clean(); runMigration() }

    suspend fun getChannels(): List<Channel> {
        return database.transaction {
            db.selectFrom(CHANNEL).fetchInto(Channel::class.java)
        }
    }
}
