package platformapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.deleteMember
import persistence.postgres.queries.updateMember
import platformapi.models.ChannelMemberWritePayload

suspend fun PipelineContext<Unit, ApplicationCall>.updateMember(
    location: ChannelList.ChannelDetails.ChannelMemberList.ChannelMemberDetails,
    database: KotlinDslContext
) {
    val member = call.receive<ChannelMemberWritePayload>()
    // TODO(saibotma): Check permission
    database.transaction {
        updateMember(
            channelId = location.channelMemberList.channelDetails.channelId,
            userId = location.userId,
            role = member.role
        )
    }
    call.respond(HttpStatusCode.NoContent)
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

