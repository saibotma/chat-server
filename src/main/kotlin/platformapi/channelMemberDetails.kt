package platformapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.deleteMember
import persistence.postgres.queries.getMembersOf
import persistence.postgres.queries.updateMember
import platformapi.models.ChannelMemberWritePayload
import platformapi.models.toChannelMemberRead

suspend fun PipelineContext<Unit, ApplicationCall>.updateMember(
    location: ChannelList.ChannelDetails.ChannelMemberList.ChannelMemberDetails,
    database: KotlinDslContext
) {
    // TODO(saibotma): Don't allow admins in a managed channel
    val member = call.receive<ChannelMemberWritePayload>()
    val channelId = location.channelMemberList.channelDetails.channelId
    val userId = location.userId
    val result = database.transaction {
        updateMember(channelId = channelId, userId = userId, role = member.role)
        getMembersOf(channelId = channelId, userIdFilter = userId).first().toChannelMemberRead()
    }
    call.respond(HttpStatusCode.OK, result)
}

suspend fun PipelineContext<Unit, ApplicationCall>.deleteChannelMember(
    location: ChannelList.ChannelDetails.ChannelMemberList.ChannelMemberDetails,
    database: KotlinDslContext
) {
    database.transaction {
        deleteMember(
            channelId = location.channelMemberList.channelDetails.channelId,
            userId = location.userId
        )
    }
    call.respond(HttpStatusCode.NoContent)
}

