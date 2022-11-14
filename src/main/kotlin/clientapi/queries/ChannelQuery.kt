package clientapi.queries

import clientapi.AuthContext
import models.DetailedChannelReadPayload
import org.jooq.impl.DSL.*
import persistence.jooq.KotlinDslContext
import persistence.jooq.tables.references.CHANNEL
import persistence.jooq.tables.references.CHANNEL_EVENT
import persistence.jooq.tables.references.CHANNEL_META_EVENT
import persistence.jooq.value
import persistence.postgres.queries.ChannelEvent
import persistence.postgres.queries.EventType
import persistence.postgres.queries.getChannelsOf
import persistence.postgres.queries.isMemberOfChannel
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
        mustIncludeEventOfType: List<EventType>,
    ): DetailedChannelRead2Payload {
        database.transaction {
            db.select(
                select(CHANNEL_EVENT.ID)
                    .from(CHANNEL_EVENT)
                    .where(
                        exists(
                            selectFrom(CHANNEL_META_EVENT).where(
                                CHANNEL_META_EVENT.CHANNEL_EVENT_ID.eq(CHANNEL_EVENT.ID)
                            )
                        ).or(
                            exists(
                                selectFrom(CHANNEL_MEMBER_EV).where(
                                    CHANNEL_META_EVENT.CHANNEL_EVENT_ID.eq(CHANNEL_EVENT.ID)
                                )
                            )
                        )
                    ).orderBy(CHANNEL_EVENT.ID.desc())
                    .limit(1),
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
