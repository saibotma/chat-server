package persistence.postgres.queries

import org.jooq.impl.DSL.*
import persistence.jooq.KotlinDslContext
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.enums.ChannelMemberRole
import persistence.jooq.jsonArrayAggNoNull
import persistence.jooq.tables.CreateMemberEvent
import persistence.jooq.tables.DeleteMemberEvent
import persistence.jooq.tables.UpdateMemberEvent
import persistence.jooq.tables.references.CHANNEL_EVENT
import persistence.jooq.tables.references.CREATE_MEMBER_EVENT
import persistence.jooq.tables.references.UPDATE_CHANNEL_META_EVENT
import persistence.jooq.tables.references.UPDATE_MEMBER_EVENT
import persistence.jooq.value
import java.time.Instant
import java.util.*

enum class EventType {
    UpdateChannelMetaEvent,
    CreateMemberEvent,
    UpdateMemberEvent,
    DeleteMemberEvent,
    CreateMessageEvent,
    UpdateMessageEvent,
    DeleteMessageEvent
}

interface ChannelEvent {
    val id: Long
    val channelId: UUID
    val createdAt: Instant
}

data class UpdateChannelMetaEvent(
    override val id: Long,
    override val channelId: UUID,
    override val createdAt: Instant,
    val name: String?,
    val description: String?,
    val creatorUserId: String?,
) : ChannelEvent

data class CreateMemberEvent(
    override val id: Long,
    override val channelId: UUID,
    override val createdAt: Instant,
    val userId: String?,
    val role: ChannelMemberRole,
    val creatorUserId: String?
) : ChannelEvent

data class UpdateMemberEvent(
    override val id: Long,
    override val channelId: UUID,
    override val createdAt: Instant,
    val sourceId: Long,
    val role: ChannelMemberRole,
    val creatorUserId: String?
) : ChannelEvent

data class DeleteMemberEvent(
    override val id: Long,
    override val channelId: UUID,
    override val createdAt: Instant,
    val sourceId: Long,
    val creatorUserId: String?
) : ChannelEvent

class CreateMessageEvent(
    override val id: Long,
    override val channelId: UUID,
    override val createdAt: Instant,
    val text: String?,
    val repliedEventId: Long?,
    val creatorUserId: String?
) : ChannelEvent

class UpdateMessageEvent(
    override val id: Long,
    override val channelId: UUID,
    override val createdAt: Instant,
    val sourceId: Long,
    val text: String?,
    val repliedEventId: Long?,
    val creatorUserId: String?
) : ChannelEvent

class DeleteMessageEvent(
    override val id: Long,
    override val channelId: UUID,
    override val createdAt: Instant
    val sourceId: Long,
) : ChannelEvent

data class TestResult(
    val updateChannelMetaEvents: List<UpdateChannelMetaEvent>,
    val createMemberEvents: List<CreateMemberEvent>,
    val updateMemberEvents: List<UpdateMemberEvent>,
    val deleteMemberEvents: List<DeleteMemberEvent>,
    val createMessageEvents: List<CreateMessageEvent>,
    val updateMessageEvents: List<UpdateMessageEvent>,
    val deleteMessageEvents: List<DeleteMessageEvent>,
)

fun KotlinTransactionContext.test(channelId: UUID, beforeId: Long): TestResult {
    val memberSinceId = db.select()

    db.select(
        jsonObject(
            TestResult::updateChannelMetaEvents.value(
                select()
                    .from(UPDATE_CHANNEL_META_EVENT)
            ),
            TestResult::createMessageEvents.value(
                select()
                    .from(CREATE_MEMBER_EVENT)
                    .innerJoin(CHANNEL_EVENT).on(CREATE_MEMBER_EVENT.CHANNEL_EVENT_ID.eq(CHANNEL_EVENT.ID))
                    .and(CHANNEL_EVENT.CHANNEL_ID.eq(channelId))
                    .and(CHANNEL_EVENT.ID.lt(beforeId))
            ),
            TestResult::createMemberEvents.value(
                select()
                    .from(CREATE_MEMBER_EVENT)
                    .innerJoin(CHANNEL_EVENT).on(CHANNEL_EVENT.ID.eq(CREATE_MEMBER_EVENT.CHANNEL_EVENT_ID))
                    .and(CHANNEL_EVENT.CHANNEL_ID.eq(channelId))
                    .
            )
        )
    )

}

fun KotlinTransactionContext.test(channelId: UUID, beforeId: Long): TestResult {
    db.select(
        jsonObject(
            TestResult::createMessageEvents.value(
                select(jsonArrayAggNoNull())
                    .from(CREATE_MEMBER_EVENT)
                    .innerJoin(CHANNEL_EVENT).on(CREATE_MEMBER_EVENT.CHANNEL_EVENT_ID.eq(CHANNEL_EVENT.ID))
                    .and(CHANNEL_EVENT.CHANNEL_ID.eq(channelId))
                    .and(CHANNEL_EVENT.ID.lt(beforeId))
            )
        )
    )

    val innerChannelEvent = CHANNEL_EVENT.`as`("innerChannelEvent")

    db.select(
        CHANNEL_EVENT.ID, `when`(
            CHANNEL_EVENT.ID.eq(1),
            select().from(CREATE_MEMBER_EVENT).where(CREATE_MEMBER_EVENT.CHANNEL_EVENT_ID.eq(CHANNEL_EVENT.ID))
        )
            .`when`(CHANNEL_EVENT.ID.eq(2), select().from(UPDATE_MEMBER_EVENT))
    )
        .from(CHANNEL_EVENT)
        .unionAll(
            // TODO(saibotma): Add when stuff here.
            select(CHANNEL_EVENT.ID)
                .from(CHANNEL_EVENT)
                .where(
                    exists(
                        select().from(innerChannelEvent).where(innerChannelEvent.ID.lt(beforeId))
                            .and(innerChannelEvent.SOURCE_ID.eq(CHANNEL_EVENT.ID))
                    )
                )
        )

}


suspend fun respond(dslContext: KotlinDslContext) {
    val result = dslContext.transaction {
        test(channelId = UUID.randomUUID(), beforeId = 100)
    }

    val events: List<ChannelEvent> =
        (result.createMessageEvents + result.deleteMessageEvents + result.updateMessageEvents).sortedBy { it.id }
}
