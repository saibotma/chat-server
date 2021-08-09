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
import persistence.postgres.mappings.detailedMessageReadToJson
import java.time.Instant
import java.util.*

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
    fun select(
        limit: Int,
        compare: Field<Instant?>.(Field<Instant?>) -> Condition
    ): SelectSeekStep1<Record1<JSON>, Instant?> {
        val outerMessage = MESSAGE.`as`("outer_message")
        val innerMessage = MESSAGE.`as`("inner_message")

        return db.select(detailedMessageReadToJson(message = outerMessage))
            .from(outerMessage)
            .where(outerMessage.ID.`in`(
                select(MESSAGE.ID)
                    .from(MESSAGE)
                    .where(MESSAGE.CHANNEL_ID.eq(channelId))
                    .andIf(byDateTime != null && byMessageId == null) { MESSAGE.CREATED_AT.compare(value(byDateTime!!)) }
                    .andIf(byDateTime == null && byMessageId != null) {
                        MESSAGE.CREATED_AT.compare(
                            select(innerMessage.CREATED_AT)
                                .from(innerMessage)
                                .where(innerMessage.ID.eq(byMessageId))
                                .asField()
                        )
                    }
                    .orderBy(if (limit < 0) MESSAGE.CREATED_AT.desc() else MESSAGE.CREATED_AT.asc())
                    .limit(kotlin.math.abs(limit))
            ))
            .orderBy(outerMessage.CREATED_AT.asc())
    }

    // One compare also is "equals" in order to include the message specified by date time or message id
    return select(limit = 0 - (previousLimit + 1)) { le(it) }
        .unionAll(select(limit = nextLimit) { gt(it) })
        .fetchInto(DetailedMessageReadPayload::class.java)
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
