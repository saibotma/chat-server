package testutil.servertest.put

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
