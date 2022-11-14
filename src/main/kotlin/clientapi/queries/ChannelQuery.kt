package clientapi.queries

import clientapi.AuthContext
import models.DetailedChannelReadPayload
import org.jooq.impl.DSL.jsonObject
import org.jooq.impl.DSL.select
import persistence.jooq.KotlinDslContext
import persistence.jooq.tables.references.CHANNEL
import persistence.jooq.value
import persistence.postgres.queries.ChannelEvent
import persistence.postgres.queries.EventType
import persistence.postgres.queries.getChannelsOf
import java.util.*

class ChannelQuery(
    private val database: KotlinDslContext,
) {
    suspend fun channels(context: AuthContext): List<DetailedChannelReadPayload> {
        return database.transaction {
            getChannelsOf(context.userId)
        }
    }

    /**
     * Returns
     */
    suspend fun channels2(
        context: AuthContext,
        userId: String,
        take: Int,
        fromChannelId: UUID,
        takeEvents: Int,
        essentialEventTypes: List<EventType>,
    ): DetailedChannelRead2Payload {
        database.transaction {
            db.select(
                jsonObject {
                    DetailedChannelRead2Payload::id.value(CHANNEL.ID),
                    DetailedChannelRead2Payload::essentialEvents.value(
                        select()
                            .from(CHA)
                    )
                }
            )
                .from(CHANNEL)
                .where(isMemberOfChannel(channelId = CHANNEL.ID, userId = userId))
            // TODO(saibotma): Sort the channels, by last activity. But what counts as activity? Am besten Teilt der Client mit, welche events dazu zählen, also welche Events auch beim Nutzer angezeigt werden.
        }
    }
}

data class DetailedChannelRead2Payload(
    val id: UUID,
    /**
     * - All "ChannelMetaUpdateEvent"s
     * - The lat
     */
    val essentialEvents: List<ChannelEvent>
)
