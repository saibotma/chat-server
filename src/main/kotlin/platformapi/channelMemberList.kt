package platformapi

import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.deleteMembersOf
import persistence.postgres.queries.insertMembers

// TODO(saibotma): Finalize API & test
suspend fun PipelineContext<Unit, ApplicationCall>.updateMembers(
    location: ChannelList.ChannelDetails.ChannelMemberList,
    database: KotlinDslContext
) {
    val members = call.receive<List<ChannelMember>>()
    database.transaction {
        deleteMembersOf(channelId = location.channelDetails.channelId)
        insertMembers(members)
    }
    call.respond(HttpStatusCode.NoContent)
}
