package persistence.postgres.queries.channelcontent

import persistence.jooq.KotlinTransactionContext
import persistence.jooq.enums.ChannelContentType
import persistence.jooq.tables.references.CHANNEL_CONTENT
import java.util.*

fun KotlinTransactionContext.insertChannelContent(channelId: UUID, type: ChannelContentType, data: Map<String, Any?>) {
db.insertInto(CHANNEL_CONTENT)
    .set(CHANNEL_CONTENT.CHANNEL_ID, channelId)
    .set(CHANNEL_CONTENT.TYPE, type)
    .set(CHANNEL_CONTENT.DATA, data)
    .execute()
}
