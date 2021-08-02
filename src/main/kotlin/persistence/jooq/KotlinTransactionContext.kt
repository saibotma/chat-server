package persistence.jooq

import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.jooq.impl.DSL

class KotlinTransactionContext(val db: DSLContext) {
    /**
     * Starts a nested transaction.
     */
    fun <T> transaction(block: suspend (KotlinDslContext).() -> T): T {
        return db.transactionResult { config ->
            val context = DSL.using(config)
            runBlocking { KotlinDslContext(context).block() }
        }
    }
}
