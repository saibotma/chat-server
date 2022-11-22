package clientapi.models

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import persistence.jooq.enums.ChannelEventType
import persistence.jooq.tables.pojos.ChannelEvent
import java.time.Instant
import java.util.*

data class ChannelEventReadPayload(
    val id: Long,
    val channelId: UUID,
    val type: ChannelEventType,
    val data: ChannelEventData,
    val createdAt: Instant,
)

fun ChannelEvent.toReadPayload(objectMapper: ObjectMapper): ChannelEventReadPayload {
    return ChannelEventReadPayload(
        id = id!!,
        channelId = channelId!!,
        type = type!!,
        data = channelEventDataFrom(objectMapper = objectMapper, type = type, json = data!!.data()),
        createdAt = createdAt!!
    )
}


private fun channelEventDataFrom(objectMapper: ObjectMapper, type: ChannelEventType, json: String): ChannelEventData {
    return when (type) {
        ChannelEventType.set_channel_name -> objectMapper.readValue<SetChannelNameEventData>(json)
        ChannelEventType.set_channel_description -> objectMapper.readValue<SetChannelDescriptionEventData>(json)
        ChannelEventType.add_member -> objectMapper.readValue<AddMemberEventData>(json)
        ChannelEventType.update_member_role -> objectMapper.readValue<UpdateMemberRoleEventData>(json)
        ChannelEventType.remove_member -> objectMapper.readValue<RemoveMemberEventData>(json)
        ChannelEventType.send_message -> objectMapper.readValue<SendMessageEventData>(json)
        ChannelEventType.update_message_text -> objectMapper.readValue<UpdateMessageTextEventData>(json)
        ChannelEventType.update_message_replied_message_id -> {
            objectMapper.readValue<UpdateMessageRepliedMessageIdEventData>(json)
        }
        ChannelEventType.delete_message -> objectMapper.readValue<DeleteMessageEventData>(json)
    }
}
