package clientapi.models

import persistence.jooq.enums.ChannelMemberRole
import util.Optional

class OptionalChannelMemberRole(value: ChannelMemberRole) : Optional<ChannelMemberRole>(value)
