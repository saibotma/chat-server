package clientapi.models

import persistence.jooq.enums.ChannelMemberRole

data class CreateChannelInput(
    val name: String?,
    val description: String?,
    val members: List<CreateChannelInputMember>,
)

data class CreateChannelInputMember(val userId: String, val role: ChannelMemberRole)
