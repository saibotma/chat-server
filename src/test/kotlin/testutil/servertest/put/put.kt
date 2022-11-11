package testutil.servertest.put

import io.ktor.client.call.body
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import testutil.servertest.ServerTestEnvironment

suspend inline fun <reified T, reified R> ServerTestEnvironment.put(
    resource: T,
    path: String,
    handleResponse: suspend HttpResponse.(T, R?) -> Unit,
): Pair<T, R?> {
    val response = client.put(path) {
        contentType(ContentType.Application.Json)
        addApiKeyAuthentication()
        setBody(resource)
    }
    val result = if (response.status.isSuccessWithContent()) response.body<R>() else null
    response.handleResponse(resource, result)
    return resource to result
}
