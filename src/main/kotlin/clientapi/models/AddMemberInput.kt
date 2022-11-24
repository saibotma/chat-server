package clientapi.models

import persistence.jooq.enums.ChannelMemberRole

data class AddMemberInput(
    val role: ChannelMemberRole,
)
