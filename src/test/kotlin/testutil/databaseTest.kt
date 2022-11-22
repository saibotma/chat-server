package testutil

import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.kodein.di.DI
import org.kodein.di.instance
import persistence.jooq.KotlinDslContext
import persistence.jooq.tables.Channel.Companion.CHANNEL
import persistence.jooq.tables.User.Companion.USER
import persistence.jooq.tables.pojos.*
import persistence.jooq.tables.references.CHANNEL_MEMBER
import persistence.jooq.tables.references.CONTACT
import persistence.jooq.tables.references.FIREBASE_PUSH_TOKEN
import persistence.jooq.tables.references.MESSAGE
import testutil.servertest.BindDependencies

fun databaseTest(
    useTransaction: Boolean = true,
    bindDependencies: BindDependencies = {},
    test: suspend DatabaseTestEnvironment.() -> Unit
) {
    val di = DI {
        setupTestDependencies()
        bindDependencies()
    }
    val flyway: Flyway by di.instance()
    val environment = DatabaseTestEnvironment(di)

    handleCleanUp(flyway)

    if (useTransaction) {
        environment.scopedTest(test)
    } else {
        runBlocking { test(environment) }
        restoreDatabase(flyway)
    }
}


open class DatabaseTestEnvironment(val di: DI) {
    val database: KotlinDslContext by di.instance()

    suspend fun getChannels(): List<Channel> {
        return database.transaction {
            db.selectFrom(CHANNEL).fetchInto(Channel::class.java)
        }
    }

    suspend fun getUsers(): List<User> {
        return database.transaction {
            db.selectFrom(USER).fetchInto(User::class.java)
        }
    }

    suspend fun getMembers(): List<ChannelMember> {
        return database.transaction {
            db.selectFrom(CHANNEL_MEMBER).fetchInto(ChannelMember::class.java)
        }
    }

    suspend fun getMessages(): List<Message> {
        return database.transaction {
            db.selectFrom(MESSAGE).fetchInto(Message::class.java)
        }
    }

    suspend fun getFirebasePushTokens(): List<FirebasePushToken> {
        return database.transaction {
            db.selectFrom(FIREBASE_PUSH_TOKEN).fetchInto(FirebasePushToken::class.java)
        }
    }

    suspend fun getContacts(): List<Contact> {
        return database.transaction {
            db.selectFrom(CONTACT).fetchInto(Contact::class.java)
        }
    }

    // region helpers

    @JvmName("scopedTestDatabase")
    fun scopedTest(block: suspend DatabaseTestEnvironment.() -> Unit) {
        return scopedTest(this, block)
    }

    protected fun <T : DatabaseTestEnvironment> scopedTest(environment: T, block: suspend T.() -> Unit) {
        val dslContext: DSLContext by di.instance()

        try {
            dslContext.transaction { config ->
                val kotlinDslContext: KotlinDslContext by di.instance()
                kotlinDslContext.overrideDSLContext = DSL.using(config)
                runBlocking { environment.block() }
                throw TestRollbackException()
            }
        } catch (e: DataAccessException) {
            if (e.cause !is TestRollbackException) throw e
        }
    }

    // endregion
}
