package models

import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import dev.saibotma.persistence.postgres.jooq.tables.pojos.User

data class DetailedChannelMember(val user: User, val member: ChannelMember)
