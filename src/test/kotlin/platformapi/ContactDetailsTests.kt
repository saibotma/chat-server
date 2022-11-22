package platformapi

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import persistence.jooq.tables.pojos.Contact
import testutil.servertest.put.upsertContact
import testutil.servertest.put.upsertUser
import testutil.servertest.serverTest

class ContactDetailsTests {
    @Nested
    inner class UpsertContactTests {
        @Test
        fun `creates a contact`() {
            serverTest {
                val (_, user1) = upsertUser()
                val (_, user2) = upsertUser()

                upsertContact(userId1 = user1!!.id, userId2 = user2!!.id)

                val actualContacts = getContacts()
                val expectedContacts = listOf(Contact(userId_1 = user1.id, userId_2 = user2.id))
                actualContacts shouldContainExactlyInAnyOrder expectedContacts
            }
        }
    }
}
