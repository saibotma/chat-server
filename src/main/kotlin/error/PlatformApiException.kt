package error

import io.ktor.http.*

data class PlatformApiError(val errorCode: Int, val message: String? = null)

data class PlatformApiException(
    val statusCode: HttpStatusCode,
    val error: PlatformApiError,
) : Exception() {
    override val message = error.toString()

    companion object
}

fun PlatformApiException.Companion.dependencyNotFound(
    propertyName: String? = null,
    propertyValue: String? = null
): PlatformApiException {
    return PlatformApiException(
        HttpStatusCode.BadRequest,
        PlatformApiError(
            1,
            if (propertyName == null) "Could not be found" else "$propertyName = $propertyValue could not be found."
        )
    )
}

fun PlatformApiException.Companion.duplicate(duplicatePropertyName: String, duplicatePropertyValue: String): PlatformApiException {
    return PlatformApiException(
        HttpStatusCode.BadRequest,
        PlatformApiError(2, "Duplicate $duplicatePropertyName = \"$duplicatePropertyValue\"")
    )
}

fun PlatformApiException.Companion.resourceNotFound(): PlatformApiException {
    return PlatformApiException(
        HttpStatusCode.NotFound,
        PlatformApiError(3, "The resource could not be found.")
    )
}

fun PlatformApiException.Companion.managedChannelHasAdmin(): PlatformApiException {
    return PlatformApiException(
        HttpStatusCode.BadRequest,
        PlatformApiError(4, "A managed channel may not have admin members.")
    )
}
