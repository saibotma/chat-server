package clientapi.mutations

import clientapi.AuthContext
import persistence.jooq.KotlinDslContext

class ContactMutation(private val database: KotlinDslContext) {
    suspend fun addContact(context: AuthContext, userId: String) {
        TODO()
    }

    suspend fun removeContact(context: AuthContext, userId: String) {
        TODO()
    }
}
