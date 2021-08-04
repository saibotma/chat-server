package platformapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.deleteMembersOf
import persistence.postgres.queries.getMembersOf
import persistence.postgres.queries.insertMember
import persistence.postgres.queries.insertMembers
import platformapi.models.ChannelMemberWritePayload
import platformapi.models.toChannelMember
import java.time.Instant.now

suspend fun PipelineContext<Unit, ApplicationCall>.createMember(
    location: ChannelList.ChannelDetails.ChannelMemberList,
    database: KotlinDslContext
) {
    val member = call.receive<ChannelMemberWritePayload>()
    database.transaction {
        insertMember(
            member.toChannelMember(
                channelId = location.channelDetails.channelId,
                addedAt = now()
            )
        )
    }
    call.respond(HttpStatusCode.Created)
}


suspend fun PipelineContext<Unit, ApplicationCall>.updateMembers(
    location: ChannelList.ChannelDetails.ChannelMemberList,
    database: KotlinDslContext
) {
    val members = call.receive<List<ChannelMemberWritePayload>>()
    val memberIds = members.map { it.userId }
    val channelId = location.channelDetails.channelId
    database.transaction {
        val currentMembers = getMembersOf(channelId)
        val currentMemberIds = currentMembers.map { it.userId!! }
        deleteMembersOf(channelId, currentMemberIds - memberIds)
        insertMembers(members.filter { !currentMemberIds.contains(it.userId) }
            .map { it.toChannelMember(channelId = channelId, addedAt = now()) })
    }
    call.respond(HttpStatusCode.NoContent)
}
