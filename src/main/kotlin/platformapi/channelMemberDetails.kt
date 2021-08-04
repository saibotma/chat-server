package platformapi

import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.deleteChannelMember
import persistence.postgres.queries.insertMembers
import platformapi.models.ChannelMemberWritePayload
import platformapi.models.toChannelMemberRecord

suspend fun PipelineContext<Unit, ApplicationCall>.upsertChannelMember(
    location: ChannelList.ChannelDetails.ChannelMemberList.ChannelMemberDetails,
    database: KotlinDslContext
) {
    val member = call.receive<ChannelMemberWritePayload>()
    database.transaction {
        insertMembers(
            listOf(
                member.toChannelMemberRecord(
                    channelId = location.channelMemberList.channelDetails.channelId,
                    userId = location.userId,
                )
            )
        )
    }
    // TODO(saibotma): Return created when created
    call.respond(HttpStatusCode.NoContent)
}

suspend fun PipelineContext<Unit, ApplicationCall>.deleteChannelMember(
    location: ChannelList.ChannelDetails.ChannelMemberList.ChannelMemberDetails,
    database: KotlinDslContext
) {
    database.transaction {
        deleteChannelMember(
            channelId = location.channelMemberList.channelDetails.channelId,
            userId = location.userId
        )
    }
    call.respond(HttpStatusCode.NoContent)
}

