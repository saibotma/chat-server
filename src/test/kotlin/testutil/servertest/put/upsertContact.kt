package testutil.servertest.put

import io.ktor.client.statement.*
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.ensureSuccess

suspend fun ServerTestEnvironment.upsertContact(
    userId1: String,
    userId2: String,
    handleResponse: suspend HttpResponse.() -> Unit = { ensureSuccess() }
) {
    put<Any?, Any?>(
        null,
        "/platform/contacts?userId1=$userId1&userId2=$userId2",
        handleResponse = { _, _ -> handleResponse() }
    )
}
