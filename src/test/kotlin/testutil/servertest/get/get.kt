package testutil.servertest.get

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import testutil.servertest.ServerTestEnvironment

suspend inline fun <reified T> ServerTestEnvironment.get(
    path: String,
    handleResponse: suspend HttpResponse.(T?) -> Unit,
): T? {
    val response = client.get(path) {
        addApiKeyAuthentication()
    }
    val result = if (response.status.isSuccess()) response.body<T>() else null
    response.handleResponse(result)
    return result
}
