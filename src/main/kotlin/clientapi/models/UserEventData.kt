package clientapi.models

interface UserEventData

data class UpdateNameEventData(val name: String) : UserEventData
