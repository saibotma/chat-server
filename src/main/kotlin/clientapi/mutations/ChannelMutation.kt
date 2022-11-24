package clientapi.mutations

import clientapi.*
import clientapi.models.*
import persistence.jooq.KotlinDslContext
import persistence.jooq.enums.ChannelMemberRole
import persistence.jooq.tables.pojos.Channel
import persistence.jooq.tables.pojos.ChannelMember
import persistence.postgres.queries.channel.hasAnotherAdmin
import persistence.postgres.queries.channel.insertChannel
import persistence.postgres.queries.channel.isAdminOfChannel
import persistence.postgres.queries.channel.updateChannel
import persistence.postgres.queries.channelmember.insertMember
import persistence.postgres.queries.channelmember.insertMembers
import persistence.postgres.queries.channelmember.updateMember
import persistence.postgres.queries.contact.areContacts
import persistence.postgres.queries.contact.isAnyNotAContact
import persistence.postgres.queries.deleteChannel
import persistence.postgres.queries.deleteMember
import persistence.postgres.queries.getChannel
import java.sql.Connection.TRANSACTION_REPEATABLE_READ
import java.time.Instant.now
import java.util.*
import java.util.UUID.randomUUID

class ChannelMutation(
    private val database: KotlinDslContext,
) {
    suspend fun createChannel(context: AuthContext, input: CreateChannelInput): Void {
        validateChannelName(input.name)
        validateChannelDescription(input.description)

        val isAnyNotAContact = database.transaction {
            isAnyNotAContact(
                userIds = input.members.map { UserId(it.userId) }.toSet(),
                of = context.userId
            )
        }
        if (isAnyNotAContact) {
            throw ClientApiException.resourceNotFound()
        }

        val id = randomUUID()
        database.transaction(TRANSACTION_REPEATABLE_READ) {
            insertChannel(
                Channel(
                    id = id,
                    name = input.name,
                    description = input.description,
                    isManaged = false,
                    createdAt = now(),
                    creatorUserId = context.userId.value,
                )
            )
            insertMembers(
                (input.members + CreateChannelInputMember(
                    userId = context.userId.value,
                    role = ChannelMemberRole.admin
                )).map {
                    ChannelMember(
                        channelId = id,
                        userId = it.userId,
                        role = it.role,
                        addedAt = now()
                    )
                })
        }

        return Void
    }

    suspend fun updateChannel(
        context: AuthContext,
        id: UUID,
        input: UpdateChannelInput,
    ): Void {
        validateChannelName(input.name?.value)
        validateChannelDescription(input.description?.value)

        val isAdminOfChannel = database.transaction { isAdminOfChannel(channelId = id, userId = context.userId) }
        if (!isAdminOfChannel) {
            throw ClientApiException.resourceNotFound()
        }

        database.transaction { updateChannel(id = id, name = input.name, description = input.description) }

        return Void
    }

    suspend fun deleteChannel(
        context: AuthContext,
        id: UUID
    ): Boolean {
        val isAdminOfChannel = database.transaction { isAdminOfChannel(channelId = id, userId = context.userId) }
        if (!isAdminOfChannel) {
            throw ClientApiException.resourceNotFound()
        }
        database.transaction { deleteChannel(id) }
        return true
    }

    suspend fun addMember(
        context: AuthContext,
        channelId: UUID,
        userId: String,
        input: AddMemberInput,
    ): Void {
        val isAdminOfChannel = database.transaction { isAdminOfChannel(channelId = channelId, userId = context.userId) }
        if (!isAdminOfChannel) {
            throw ClientApiException.resourceNotFound()
        }

        val areContacts = database.transaction { areContacts(userId1 = context.userId, userId2 = UserId(userId)) }
        if (!areContacts) {
            throw ClientApiException.resourceNotFound()
        }

        database.transaction { insertMember(channelId = channelId, userId = userId, role = input.role) }

        return Void
    }

    suspend fun updateMember(
        context: AuthContext,
        channelId: UUID,
        userId: String,
        input: UpdateMemberInput,
    ): Void {
        val contextUserId = context.userId
        val isAdminOfChannel = database.transaction { isAdminOfChannel(channelId = channelId, userId = contextUserId) }
        if (!isAdminOfChannel) {
            throw ClientApiException.resourceNotFound()
        }

        database.transaction(TRANSACTION_REPEATABLE_READ) {
            if (input.role != null && userId == context.userId.value && input.role.value != ChannelMemberRole.admin) {
                val hasMoreThanOneAdmin = hasAnotherAdmin(channelId = channelId, adminId = context.userId)
                if (!hasMoreThanOneAdmin) {
                    throw ClientApiException.channelMustHaveOneAdmin()
                }
            }

            updateMember(channelId = channelId, userId = userId, role = input.role)
        }

        return Void
    }

    suspend fun removeMember(
        context: AuthContext,
        channelId: UUID,
        userId: String,
    ): Boolean {
        val isAdminOfChannel = database.transaction { isAdminOfChannel(channelId = channelId, userId = context.userId) }
        if (!isAdminOfChannel && userId != context.userId.value) {
            throw ClientApiException.resourceNotFound()
        }

        database.transaction(TRANSACTION_REPEATABLE_READ) {
            val channel = getChannel(channelId = channelId) ?: throw ClientApiException.resourceNotFound()
            if (channel.isManaged!!) {
                throw ClientApiException.resourceNotFound()
            }

            val hasMoreThanOneAdmin = hasAnotherAdmin(channelId = channelId, adminId = context.userId)
            if (!hasMoreThanOneAdmin) {
                throw ClientApiException.channelMustHaveOneAdmin()
            }

            deleteMember(channelId = channelId, userId = userId)
        }
        return true
    }
}

private fun validateChannelName(name: String?) {
    if (name?.isBlank() == true) {
        throw ClientApiException.channelNameBlank()
    }
}

private fun validateChannelDescription(description: String?) {
    if (description?.isBlank() == true) {
        throw ClientApiException.channelDescriptionBlank()
    }
}
