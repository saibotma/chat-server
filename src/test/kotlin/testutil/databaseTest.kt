package testutil

import dev.saibotma.persistence.postgres.jooq.tables.pojos.Channel
import di.setupKodein
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import persistence.postgres.ChatServerPostgres
import platformapi.setupTestDependencies

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

    suspend fun getChannels(): Flow<Channel> {
        return database.transaction {
            db.selectFrom(dev.saibotma.persistence.postgres.jooq.tables.Channel.CHANNEL).asFlow().map { it.into(Channel::class.java) }
        }
    }
}
