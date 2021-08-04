package clientapi

import com.expediagroup.graphql.generator.execution.GraphQLContext
import dev.saibotma.persistence.postgres.jooq.tables.pojos.User
import io.ktor.auth.*
import java.util.*

class AuthContext(
    val userId: String,
    val jwtToken: String,
) : GraphQLContext, Principal
