package clientapi

data class ClientApiException(val error: ClientApiError) : Exception() {
    override val message = error.toString()

    companion object
}

fun ClientApiException.Companion.resourceNotFound(): ClientApiException {
    return ClientApiException(ClientApiError(0, "The resource could not be found."))
}
