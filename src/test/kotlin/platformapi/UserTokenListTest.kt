package platformapi

import clientapi.ClientApiConfig
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.direct
import org.kodein.di.instance
import testutil.serverTest

class UserTokenListTest {
    @Nested
    inner class CreateUserTokenTests {
        @Test
        fun `returns a user token`() {
            serverTest {
                val jwtSecret = di.direct.instance<ClientApiConfig>().jwtSecret
                val (_, user) = createUser()
                val (_, token) = createUserToken(userId = user!!.id)

                with(JWT.decode(token!!.jwt)) {
                    subject shouldBe user.id
                    expiresAt shouldNotBe null
                    shouldNotThrow<JWTVerificationException> {
                        JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(this)
                    }
                }
            }
        }

        @Test
        fun `returns an error when the user does not exist`() {
            serverTest {
                createUserToken(userId = "not existing id") { _, _ ->
                    ensureResourceNotFound()
                }
            }
        }
    }
}
