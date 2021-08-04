package clientapi.models

import models.DetailedChannelMember
import java.util.*

data class DetailedChannel(
    val id: UUID,
    val name: String?,
    val isManaged: Boolean,
    val members: List<DetailedChannelMember>,
)
