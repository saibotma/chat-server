package clientapi.queries

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.models.*
import clientapi.resourceNotFound
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import persistence.jooq.KotlinDslContext
import persistence.jooq.enums.ChannelEventType
import persistence.jooq.enums.ChannelMemberRole
import persistence.postgres.queries.channelevent.getChannelEvents
import persistence.postgres.queries.channelmember.isMemberOfChannel
import java.util.*

class ChannelEventQuery(private val database: KotlinDslContext, private val objectMapper: ObjectMapper) {
    suspend fun channelEvents(
        context: AuthContext,
        channelId: UUID,
        beforeId: Long,
        take: Int,
    ): List<ChannelEventReadPayload> {
        val isMemberOfChannel =
            database.transaction { isMemberOfChannel(channelId = channelId, userId = context.userId) }
        if (!isMemberOfChannel) throw ClientApiException.resourceNotFound()

        val rawEvents = database.transaction { getChannelEvents(beforeId = beforeId, take = take) }
        return rawEvents.map { event ->
            ChannelEventReadPayload(
                id = event.id!!,
                channelId = event.channelId!!,
                type = event.type!!,
                data = channelEventDataFrom(type = event.type, json = event.data!!),
                createdAt = event.createdAt!!
            )
        }
    }
}

fun channelEventDataFrom(type: ChannelEventType, json: String): ChannelEventData {
    fun getUserId() = json["user_id"] as String
    fun getRole() = ChannelMemberRole.valueOf(json["role"] as String)

    fun getMessageId() = UUID.fromString(json["message_id"] as String)
    fun getText() = json["text"] as String
    fun getRepliedMessageId() = (json["replied_message_id"] as String?)?.let(UUID::fromString)
    fun getCreatorUserId() = json["creator_user_id"] as String?

    return when (type) {
        ChannelEventType.set_channel_name -> {
            SetChannelNameEventData(name = json["name"] as String)
        }
        ChannelEventType.set_channel_description -> {
            SetChannelDescriptionEventData(description = json["description"] as String)
        }
        ChannelEventType.add_member -> {
            AddMemberEventData(userId = getUserId(), role = getRole())
        }
        ChannelEventType.update_member_role -> {
            UpdateMemberRoleEventData(userId = getUserId(), role = getRole())
        }
        ChannelEventType.remove_member -> {
            RemoveMemberEventData(userId = getUserId())
        }
        ChannelEventType.send_message -> {
            SendMessageEventData(
                messageId = getMessageId(),
                text = getText(),
                repliedMessageId = getRepliedMessageId(),
                creatorUserId = getCreatorUserId()
            )
        }
        ChannelEventType.update_message_text -> {
            UpdateMessageTextEventData(messageId = getMessageId(), text = getText())
        }
        ChannelEventType.update_message_replied_message_id -> {
            UpdateMessageRepliedMessageIdEventData(messageId = getMessageId(), repliedMessageId = getRepliedMessageId())
        }
        ChannelEventType.delete_message -> {
            DeleteMessageEventData(messageId = getMessageId())
        }
    }
}
