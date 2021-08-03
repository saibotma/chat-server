package persistence.postgres.queries

import clientapi.models.DetailedMessage
import dev.saibotma.persistence.postgres.jooq.tables.Message.Companion.MESSAGE
import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import dev.saibotma.persistence.postgres.jooq.tables.pojos.Message
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelMemberRecord
import dev.saibotma.persistence.postgres.jooq.tables.records.MessageRecord
import org.jooq.impl.DSL.select
import persistence.jooq.KotlinTransactionContext
import persistence.postgres.mappings.detailedMessageToJson
import java.util.*

fun KotlinTransactionContext.getMessagesOf(channelId: UUID, userId: String): List<DetailedMessage> {
    return db.select(detailedMessageToJson(message = MESSAGE))
        .from(MESSAGE)
        .where(MESSAGE.CHANNEL_ID.eq(channelId))
        .and(isMemberOfChannel(channelId = MESSAGE.CHANNEL_ID, userId = userId))
        .fetchInto(DetailedMessage::class.java)
}

fun KotlinTransactionContext.insertMessage(message: Message) {
    db.insertInto(MESSAGE)
        .set(MessageRecord().apply { from(message) })
        .execute()
}

fun KotlinTransactionContext.updateMessage(
    messageId: UUID,
    text: String,
    respondedMessageId: UUID?,
    extendedMessageId: UUID?
) {
    db.update(MESSAGE)
        .set(MESSAGE.TEXT, text)
        .set(MESSAGE.RESPONDED_MESSAGE_ID, respondedMessageId)
        .set(MESSAGE.EXTENDED_MESSAGE_ID, extendedMessageId)
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
