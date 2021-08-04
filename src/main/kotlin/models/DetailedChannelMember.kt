package models

import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import dev.saibotma.persistence.postgres.jooq.tables.pojos.User
import java.time.Instant
import java.util.*

data class DetailedChannelMember(val channelId: UUID, val user: User, val role: ChannelMemberRole, val addedAt: Instant)
