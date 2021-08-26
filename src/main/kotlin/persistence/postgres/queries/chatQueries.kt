package persistence.postgres.queries

import clientapi.models.DetailedMessageReadPayload
import persistence.jooq.tables.Message.Companion.MESSAGE
import persistence.jooq.tables.pojos.ChannelMember
import persistence.jooq.tables.pojos.Message
import persistence.jooq.tables.records.ChannelMemberRecord
import persistence.jooq.tables.records.MessageRecord
import org.jooq.*
import org.jooq.impl.DSL.*
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.andIf
import persistence.jooq.valueNull
import persistence.postgres.mappings.detailedMessageReadToJson
import java.time.Instant
import java.util.*
import kotlin.math.abs

fun KotlinTransactionContext.getMessage(id: UUID): DetailedMessageReadPayload? {
    return db.select(detailedMessageReadToJson(message = MESSAGE))
        .from(MESSAGE)
        .where(MESSAGE.ID.eq(id))
        .fetchOneInto(DetailedMessageReadPayload::class.java)
}

fun KotlinTransactionContext.getMessagesOf(
    channelId: UUID,
    byDateTime: Instant?,
    byMessageId: UUID?,
    previousLimit: Int,
    nextLimit: Int,
): List<DetailedMessageReadPayload> {
    return selectMessagesOf(
        channelId = value(channelId),
        byDateTime = valueNull(byDateTime),
        byMessageId = valueNull(byMessageId),
        previousLimit = previousLimit,
        nextLimit = nextLimit,
    ).apply { attach(db.configuration()) }
        .fetchInto(DetailedMessageReadPayload::class.java)
}

/**
 * Returns messages of [channelId].
 * When [byDateTime] is set then messages that are before or after this value.
 * When [byMessageId] is set then messages that are before of after this message.
 * When none of both is set then either [previousLimit] or [nextLimit] have to be 0 and
 * then returns the messages starting from the start or the end.
 * If none of both is 0 then the behaviour is undefined.
 */
fun selectMessagesOf(
    channelId: Field<UUID?>,
    byDateTime: Field<Instant?>?,
    byMessageId: Field<UUID?>?,
    previousLimit: Int,
    nextLimit: Int,
    aliasName: Name = name(::DetailedMessageReadPayload.name),
): SelectOrderByStep<Record1<JSON>> {
    fun select(
        limit: Int,
        compare: Field<Instant?>.(Field<Instant?>) -> Condition
    ): SelectSeekStep2<Record1<JSON>, Instant?, UUID?> {
        val outerMessage = MESSAGE.`as`("outer_message")
        val innerMessage = MESSAGE.`as`("inner_message")

        return select(detailedMessageReadToJson(message = outerMessage).`as`(aliasName))
            .from(outerMessage)
            .where(outerMessage.ID.`in`(
                select(MESSAGE.ID)
                    .from(MESSAGE)
                    .where(MESSAGE.CHANNEL_ID.eq(channelId))
                    .andIf(byDateTime != null && byMessageId == null) { MESSAGE.CREATED_AT.compare(byDateTime!!) }
                    .andIf(byDateTime == null && byMessageId != null) {
                        MESSAGE.CREATED_AT.compare(
                            select(innerMessage.CREATED_AT)
                                .from(innerMessage)
                                .where(innerMessage.ID.eq(byMessageId))
                                .asField()
                        )
                    }
                    .orderBy(if (limit < 0) MESSAGE.CREATED_AT.desc() else MESSAGE.CREATED_AT.asc())
                    .limit(abs(limit))
            ))
            // Fallback ordering by id to ensure deterministic results.
            .orderBy(outerMessage.CREATED_AT.asc(), outerMessage.ID)
    }

    // One compare also is "equals" in order to include the message specified by date time or message id
    return select(limit = 0 - (previousLimit + if (byDateTime != null || byMessageId != null) 1 else 0)) { le(it) }
        .unionAll(select(limit = nextLimit) { gt(it) })
}

fun KotlinTransactionContext.insertMessage(message: Message) {
    db.insertInto(MESSAGE)
        .set(MessageRecord().apply { from(message) })
        .execute()
}

fun KotlinTransactionContext.updateMessage(
    messageId: UUID,
    text: String?,
) {
    db.update(MESSAGE)
        .set(MESSAGE.TEXT, text)
        .where(MESSAGE.ID.eq(messageId))
        .execute()
}

fun KotlinTransactionContext.deleteMessage(id: UUID) {
    db.deleteFrom(MESSAGE)
        .where(MESSAGE.ID.eq(id))
        .execute()
}

fun KotlinTransactionContext.isCreatorOfMessage(messageId: UUID, userId: String): Boolean {
    return db.fetchExists(
        select()
            .from(MESSAGE)
            .where(MESSAGE.CREATOR_USER_ID.eq(userId))
            .and(MESSAGE.ID.eq(messageId))
    )
}

fun KotlinTransactionContext.insertChatRoomMembers(members: List<ChannelMember>) {
    db.batchInsert(members.map { ChannelMemberRecord().apply { from(it) } }).execute()
}

fun KotlinTransactionContext.deleteChatRoomMembers(members: List<ChannelMember>) {
    db.batchDelete(members.map { ChannelMemberRecord().apply { from(it) } }).execute()
}
