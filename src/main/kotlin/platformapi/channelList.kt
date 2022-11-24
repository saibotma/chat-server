package platformapi

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import models.ChannelWritePayload
import models.toChannel
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.channel.insertChannel
import persistence.postgres.queries.getChannel
import java.time.Instant
import java.util.UUID.randomUUID

suspend fun PipelineContext<Unit, ApplicationCall>.createChannel(
    location: ChannelList,
    database: KotlinDslContext
) {
    val channel = call.receive<ChannelWritePayload>()
    val result = database.transaction {
        val channelId = randomUUID()
        insertChannel(channel.toChannel(id = channelId, createdAt = Instant.now()))
        getChannel(channelId)
    }
    call.respond(HttpStatusCode.Created, result!!)
}
