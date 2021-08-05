package platformapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import platformapi.models.ChannelMemberWritePayload
import platformapi.models.toChannelMember
import platformapi.models.toChannelMemberWrite
import java.time.Instant.now

suspend fun PipelineContext<Unit, ApplicationCall>.addMember(
    location: ChannelList.ChannelDetails.ChannelMemberList,
    database: KotlinDslContext
) {
    val member = call.receive<ChannelMemberWritePayload>()
    val channelId = location.channelDetails.channelId
    val result = database.transaction {
        insertMember(
            member.toChannelMember(
                channelId = channelId,
                addedAt = now()
            )
        )
        getMembersOf(channelId = channelId, userIdFilter = member.userId).first()
    }
    call.respond(HttpStatusCode.Created, result)
}

// TODO(saibotma): Check that ther must be at least one admin when not managed
// and that there must not be an admin when managed
suspend fun PipelineContext<Unit, ApplicationCall>.updateMembers(
    location: ChannelList.ChannelDetails.ChannelMemberList,
    database: KotlinDslContext
) {
    val channelId = location.channelDetails.channelId
    val members = call.receive<Array<ChannelMemberWritePayload>>()
    val result = database.transaction {
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
