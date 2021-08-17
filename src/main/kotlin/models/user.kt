package models

import persistence.jooq.tables.pojos.User
import java.time.Instant

interface UserPayload {
    val name: String?
}

data class UserWritePayload(override val name: String?) : UserPayload
data class UserReadPayload(val id: String, override val name: String?, val createdAt: Instant) : UserPayload

data class DetailedUserReadPayload(val id: String, override val name: String?, val createdAt: Instant) :
    UserPayload

fun UserWritePayload.toUser(id: String, createdAt: Instant): User {
    return User(id = id, name = name, createdAt = createdAt)
}

fun User.toUserRead(): UserReadPayload {
    return UserReadPayload(id = id!!, name = name, createdAt = createdAt!!)
}

fun UserReadPayload.toDetailed(): DetailedUserReadPayload {
    return DetailedUserReadPayload(
        id = id,
        name = name,
        createdAt = createdAt,
    )
}
