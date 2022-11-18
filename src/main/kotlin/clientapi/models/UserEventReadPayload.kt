package clientapi.models;

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import persistence.jooq.enums.UserEventType
import persistence.jooq.tables.pojos.UserEvent
import java.time.Instant

data class UserEventReadPayload(
    val id: Long,
    val userId: String,
    val type: UserEventType,
    val data: UserEventData,
    val createdAt: Instant,
)

fun UserEvent.toReadPayload(objectMapper: ObjectMapper): UserEventReadPayload {
    return UserEventReadPayload(
        id = id!!,
        userId = userId!!,
        type = type!!,
        data = userEventDataFrom(objectMapper = objectMapper, type = type, json = data!!.data()),
        createdAt = createdAt!!
    )
}

private fun userEventDataFrom(objectMapper: ObjectMapper, type: UserEventType, json: String): UserEventData {
    return when (type) {
        UserEventType.update_name -> objectMapper.readValue<UpdateNameEventData>(json)
    }
}

