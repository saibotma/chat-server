package testutil.servertest.delete

import io.ktor.client.statement.*
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.ensureNoContent

suspend fun ServerTestEnvironment.deleteUser(
    id: String,
    handleResponse: suspend HttpResponse.() -> Unit = { ensureNoContent() },
) {
    delete(path = "/platform/users/$id", handleResponse = handleResponse)
}
