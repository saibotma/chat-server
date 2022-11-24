package clientapi

import com.expediagroup.graphql.generator.execution.GraphQLContext
import io.ktor.server.auth.*

class AuthContext(val userId: UserId) : GraphQLContext, Principal
