package clientapi.models

data class UpdateChannelInput(
    val name: OptionalNullableString? = null,
    val description: OptionalNullableString? = null,
)
