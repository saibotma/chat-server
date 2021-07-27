package app.appella.persistence.jooq

import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.jooq.impl.DSL
import persistence.jooq.KotlinDslContext

class KotlinTransactionContext(val db: DSLContext) {
    /**
     * Starts a nested transaction.
     */
    suspend fun <T> transaction(block: suspend (KotlinDslContext).() -> T): T {
        return db.transactionResultAsync { config ->
            val context = DSL.using(config)
            runBlocking { KotlinDslContext(context).block() }
        }.await()
    }
}
