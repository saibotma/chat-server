package platformapi.models

import java.util.*

data class ChannelMeta(
    val id: UUID,
    val name: String?,
    val isManaged: Boolean,
)
