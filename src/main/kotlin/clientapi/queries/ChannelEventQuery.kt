package clientapi.queries

import clientapi.AuthContext
import org.jooq.Condition
import org.jooq.Field
import org.jooq.Record1
import org.jooq.SelectSelectStep
import org.jooq.impl.DSL.*
import persistence.jooq.KotlinDslContext
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.CHANNEL_EVENT
import persistence.jooq.tables.references.CREATE_MEMBER_EVENT
import persistence.jooq.tables.references.DELETE_MEMBER_EVENT
import persistence.jooq.tables.references.UPDATE_MEMBER_EVENT
import persistence.postgres.queries.ChannelEvent
import java.util.*

class ChannelEventQuery(private val database: KotlinDslContext) {
    suspend fun messages2(
        context: AuthContext,
        channelId: UUID,
        byEventId: Long,
        take: Int,
    ): List<ChannelEventReadPayload> {

        TODO()
    }
}

data class ChannelEventReadPayload(val event: ChannelEvent)

fun KotlinTransactionContext.isMemberOfChannel(channelId: Field<UUID?>, userId: String): Condition {
    return exists(
        select().from(CREATE_MEMBER_EVENT)
            .innerJoin(CHANNEL_EVENT).on(CHANNEL_EVENT.ID.eq(CREATE_MEMBER_EVENT.CHANNEL_EVENT_ID))
            .where(CREATE_MEMBER_EVENT.USER_ID.eq(userId))
    )
        .and(
            notExists(
                select().from(DELETE_MEMBER_EVENT)
                    .innerJoin(CHANNEL_EVENT).on(CHANNEL_EVENT.ID.eq(UPDATE_MEMBER_EVENT.CHANNEL_EVENT_ID))
                    .where(
                        DELETE_MEMBER_EVENT.CHANNEL_EVENT_ID.gt(
                            select(CREATE_MEMBER_EVENT.CHANNEL_EVENT_ID)
                                .from(CREATE_MEMBER_EVENT)
                                .innerJoin(CHANNEL_EVENT)
                                .on(CHANNEL_EVENT.ID.eq(CREATE_MEMBER_EVENT.CHANNEL_EVENT_ID))
                                .orderBy(CREATE_MEMBER_EVENT.CHANNEL_EVENT_ID.desc())
                                .limit(1)
                        )
                    )
            )
        )
}
