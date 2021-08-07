package platformapi

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import models.UserReadPayload
import models.UserWritePayload
import models.toUserRead
import testutil.serverTest

class UserDetailsTests {
    @Nested
    inner class CreateUserTests {
        @Test
        fun `creates a user and returns it`() {
            serverTest {
                val (write, read) = createUser()
                read?.toWrite() shouldBe write

                with(getUsers().map { it.toUserRead() }) {
                    shouldHaveSize(1)
                    first().shouldBe(read)
                }
            }
        }
    }

    @Nested
    inner class DeleteUserTests {
        @Test
        fun `deletes a user`() {
            serverTest {
                val (_, user1) = createUser()
                val (_, user2) = createUser()
                deleteUser(id = user1!!.id)

                with(getUsers()) {
                    shouldHaveSize(1)
                    first().id shouldBe user2!!.id
                }
            }
        }
    }
}

private fun UserReadPayload.toWrite(): UserWritePayload {
    return UserWritePayload(id = id, name = name)
}
