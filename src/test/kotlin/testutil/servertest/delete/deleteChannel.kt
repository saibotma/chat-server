package testutil.servertest.delete

import io.ktor.client.statement.*
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.ensureNoContent
import java.util.*

suspend fun ServerTestEnvironment.deleteChannel(
    id: UUID,
    handleResponse: suspend HttpResponse.() -> Unit = { ensureNoContent() }
) {
    delete(path = "/platform/channels/$id", handleResponse = handleResponse)
}
