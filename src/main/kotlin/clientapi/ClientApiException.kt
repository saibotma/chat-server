package clientapi


data class ClientApiException(val error: ClientApiError) : Exception() {
    override val message = error.toString()

    companion object
}

fun ClientApiException.Companion.resourceNotFound(): ClientApiException {
    return ClientApiException(ClientApiError(type = ClientApiErrorType.ResourceNotFound))
}

fun ClientApiException.Companion.channelNameBlank(): ClientApiException {
    return ClientApiException(ClientApiError(type = ClientApiErrorType.ChannelNameBlank))
}

fun ClientApiException.Companion.channelDescriptionBlank(): ClientApiException {
    return ClientApiException(ClientApiError(type = ClientApiErrorType.ChannelDescriptionBlank))
}

fun ClientApiException.Companion.channelMustHaveOneAdmin(): ClientApiException {
    return ClientApiException(ClientApiError(type = ClientApiErrorType.ChannelMustHaveOneAdmin))
}
