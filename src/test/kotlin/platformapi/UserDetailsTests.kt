package platformapi

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import models.UserReadPayload
import models.UserWritePayload
import models.toUserRead
import testutil.mockedUser
import testutil.serverTest

class UserDetailsTests {
    @Nested
    inner class UpsertUserTests {
        @Test
        fun `creates a user and returns it`() {
            serverTest {
                val (write, read) = upsertUser()
                read?.toWrite() shouldBe write

                with(getUsers().map { it.toUserRead() }) {
                    shouldHaveSize(1)
                    first().shouldBe(read)
                }
            }
        }

        @Test
        fun `updates a user and returns it`() {
            serverTest {
                // Add other user to check that only the specified one gets updated
                val (_, otherUser) = upsertUser()
                val (write, read) = upsertUser(user = mockedUser())
                val (updatedWrite, updatedRead) = upsertUser(id = read!!.id, write.copy(name = "updated name"))
                updatedRead?.toWrite() shouldBe updatedWrite

                getUsers().map { it.toUserRead() } shouldContainExactlyInAnyOrder listOf(otherUser, updatedRead)
            }
        }
    }

    @Nested
    inner class DeleteUserTests {
        @Test
        fun `deletes a user`() {
            serverTest {
                val (_, user1) = upsertUser()
                val (_, user2) = upsertUser()
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
    return UserWritePayload(name = name)
}
