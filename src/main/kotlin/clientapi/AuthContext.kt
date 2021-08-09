package clientapi

import com.expediagroup.graphql.generator.execution.GraphQLContext
import io.ktor.auth.*

class AuthContext(val userId: String) : GraphQLContext, Principal
