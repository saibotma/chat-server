package testutil.servertest.delete

import io.ktor.client.statement.*
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.ensureNoContent
import java.util.*

suspend fun ServerTestEnvironment.deleteMember(
    channelId: UUID,
    userId: String,
    response: suspend HttpResponse.() -> Unit = { ensureNoContent() }
) {
    delete(path = "/platform/channels/$channelId/members/$userId", handleResponse = response)
}
