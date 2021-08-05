package platformapi

import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import error.ApiException
import error.managedChannelHasAdmin
import error.resourceNotFound
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import models.ChannelMemberWritePayload
import models.toChannelMember
import models.toChannelMemberWrite
import java.time.Instant.now

suspend fun PipelineContext<Unit, ApplicationCall>.addMember(
    location: ChannelList.ChannelDetails.ChannelMemberList,
    database: KotlinDslContext
) {
    val member = call.receive<ChannelMemberWritePayload>()
    val channelId = location.channelDetails.channelId
    val result = database.transaction {
        val channel = getChannel(channelId = channelId) ?: throw ApiException.resourceNotFound()
        if (channel.isManaged!! && member.role == ChannelMemberRole.admin) {
            throw ApiException.managedChannelHasAdmin()
        }

        insertMember(member.toChannelMember(channelId = channelId, addedAt = now()))
        getMembersOf(channelId = channelId, userIdFilter = member.userId).first()
    }
    call.respond(HttpStatusCode.Created, result)
}

suspend fun PipelineContext<Unit, ApplicationCall>.updateMembers(
    location: ChannelList.ChannelDetails.ChannelMemberList,
    database: KotlinDslContext
) {
    val channelId = location.channelDetails.channelId
    val members = call.receive<Array<ChannelMemberWritePayload>>()
    val result = database.transaction {
        val channel = getChannel(channelId = channelId) ?: throw ApiException.resourceNotFound()
        if (channel.isManaged!! && members.any { it.role == ChannelMemberRole.admin }) {
            throw ApiException.managedChannelHasAdmin()
        }

        val currentMembers = getMembersOf(channelId).map { it.toChannelMemberWrite() }

        val union = members.map { it.userId }.intersect(currentMembers.map { it.userId }).toList()
        val deleted = currentMembers.filter { !union.contains(it.userId) }.map { it.userId }
        val inserted = members.filter { member -> !currentMembers.map { it.userId }.contains(member.userId) }
            .map { it.toChannelMember(channelId, addedAt = now()) }
        val updated = members.filter { union.contains(it.userId) }

        deleteMembersOf(channelId, deleted)
        insertMembers(inserted)
        updateMembers(channelId = channelId, members = updated)
        getMembersOf(channelId = channelId)
    }
    call.respond(HttpStatusCode.OK, result)
}
