package persistence.postgres.queries

import clientapi.models.DetailedMessage
import dev.saibotma.persistence.postgres.jooq.tables.Message.Companion.MESSAGE
import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import dev.saibotma.persistence.postgres.jooq.tables.pojos.Message
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelMemberRecord
import dev.saibotma.persistence.postgres.jooq.tables.records.MessageRecord
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL_MEMBER
import org.jooq.*
import org.jooq.impl.DSL.*
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.funAlias
import persistence.postgres.mappings.detailedChannelToJson
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

private fun KotlinTransactionContext.selectChannelsOf(
    accountMemberId: UUID,
    chatRoomIdFilter: UUID? = null
): SelectConditionStep<Record1<JSON>> {
    return db.select(detailedChannelToJson(channel = CHANNEL))
        .from(CHAT_ROOM)
        .leftJoin(COURSE).on(COURSE.CHAT_ROOM_ID.eq(CHAT_ROOM.ID))
        .where(isMemberOfChannel(channelId = CHAT_ROOM.ID, userId = accountMemberId))
        .andIf(chatRoomIdFilter != null) { CHAT_ROOM.ID.eq(chatRoomIdFilter) }
}

fun KotlinTransactionContext.isMemberOfChannel(channelId: UUID, userId: String): Boolean {
    return db.select(field(isMemberOfChannel(channelId = value(channelId), userId = userId)))
        .fetchOneInto(Boolean::class.java) ?: false
}

fun KotlinTransactionContext.isCreatorOfMessage(messageId: UUID, userId: String): Boolean {
    return db.fetchExists(
        select()
            .from(MESSAGE)
            .where(MESSAGE.CREATOR_USER_ID.eq(userId))
            .and(MESSAGE.ID.eq(messageId))
    )
}

private fun selectUserIdsOfChannel(channelId: Field<UUID?>): SelectConditionStep<Record1<String?>> {
    val funName = ::selectUserIdsOfChannel.name
    val channelMemberAlias = CHANNEL_MEMBER.funAlias(funName)
    return select(channelMemberAlias.USER_ID)
        .from(channelMemberAlias)
        .where(channelMemberAlias.CHANNEL_ID.eq(channelId))
}

private fun isMemberOfChannel(channelId: Field<UUID?>, userId: String): Condition {
    return value(userId).`in`(selectUserIdsOfChannel(channelId = channelId))
}

fun KotlinTransactionContext.insertChatRoomMembers(members: List<ChannelMember>) {
    db.batchInsert(members.map { ChannelMemberRecord().apply { from(it) } }).execute()
}

fun KotlinTransactionContext.deleteChatRoomMembers(members: List<ChannelMember>) {
    db.batchDelete(members.map { ChannelMemberRecord().apply { from(it) } }).execute()
}
