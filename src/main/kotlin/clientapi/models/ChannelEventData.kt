package clientapi.models

import persistence.jooq.enums.ChannelMemberRole
import java.util.*


interface ChannelEventData

data class SetChannelNameEventData(val name: String) : ChannelEventData
data class SetChannelDescriptionEventData(val description: String) : ChannelEventData

interface UserReferenceEventData : ChannelEventData {
    val userId: String
}

data class AddMemberEventData(override val userId: String, val role: ChannelMemberRole) : UserReferenceEventData
data class UpdateMemberRoleEventData(override val userId: String, val role: ChannelMemberRole) : UserReferenceEventData
data class RemoveMemberEventData(override val userId: String) : UserReferenceEventData

interface MessageReferenceEventData : ChannelEventData {
    val messageId: UUID
}

data class SendMessageEventData(
    override val messageId: UUID,
    val text: String,
    val repliedMessageId: UUID?,
    val creatorUserId: String?
) : MessageReferenceEventData

data class UpdateMessageTextEventData(override val messageId: UUID, val text: String) : MessageReferenceEventData
data class UpdateMessageRepliedMessageIdEventData(override val messageId: UUID, val repliedMessageId: UUID?) :
    MessageReferenceEventData

data class DeleteMessageEventData(override val messageId: UUID) : MessageReferenceEventData
