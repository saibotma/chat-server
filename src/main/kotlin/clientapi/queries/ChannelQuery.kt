package clientapi.queries

import clientapi.AuthContext
import clientapi.models.DetailedChannel
import persistence.jooq.KotlinDslContext

class ChannelQuery(
    private val database: KotlinDslContext,
) {
    fun channels(context: AuthContext): List<DetailedChannel> {
        TODO()
    }
}
