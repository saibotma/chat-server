package clientapi.models

import dev.saibotma.persistence.postgres.jooq.tables.pojos.User
import java.util.*

data class DetailedMessage(
    val id: UUID,
    val text: String,
    val respondedMessageId: UUID?,
    val extendedMessageId: UUID?,
    val creator: User?
)
