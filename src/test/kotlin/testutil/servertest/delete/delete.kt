package testutil.servertest.delete

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.statement.HttpResponse
import io.ktor.http.URLProtocol
import testutil.servertest.ServerTestEnvironment

suspend fun ServerTestEnvironment.delete(
    path: String,
    handleResponse: suspend HttpResponse.() -> Unit,
) {
    val response = client.delete(path) {
        addApiKeyAuthentication()
    }
    response.handleResponse()
}
