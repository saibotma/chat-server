package platformapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import platformapi.models.ChannelWritePayload
import platformapi.models.toChannelRecord
import java.time.Instant
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.createChannel(
    location: ChannelList,
    database: KotlinDslContext
) {
    val channel = call.receive<ChannelWritePayload>()
    database.transaction {
        insertChannel(channel.toChannelRecord(id = UUID.randomUUID(), createdAt = Instant.now()))
    }
    call.respond(HttpStatusCode.Created)
}
