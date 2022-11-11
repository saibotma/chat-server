package testutil.servertest

import error.PlatformApiError
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

suspend fun HttpResponse.asApiError(): PlatformApiError = body()
