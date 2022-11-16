package clientapi.models;

import persistence.jooq.enums.UserEventType
import java.time.Instant

data class UserEventReadPayload(
    val id: Long,
    val userId: String,
    val type: UserEventType,
    val data: Map<String, Any?>,
    val createdAt: Instant,
)
