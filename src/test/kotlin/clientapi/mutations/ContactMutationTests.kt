package clientapi.mutations

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import persistence.jooq.tables.pojos.Contact
import testutil.ignoreUnknown
import testutil.servertest.put.upsertUser
import testutil.servertest.serverTest

class ContactMutationTests {
    @Nested
    inner class AddContactTests {
        @Test
        fun `adds a new unapproved contact when no unapproved entry from the other side exists yet`() {
            serverTest {
                val (_, user1) = upsertUser()
                val (_, user2) = upsertUser()

                contactMutation.addContact(context = user1!!.toAuthContext(), userId = user2!!.id)

                val actualContacts = getContacts().ignoreUnknown()
                val expectedContacts =
                    listOf(Contact(userId_1 = user1.id, userId_2 = user2.id, isApproved = false)).ignoreUnknown()
                actualContacts shouldContainExactlyInAnyOrder expectedContacts
            }
        }

        @Test
        fun `approves the contact when an unapproved entry from the other side exists yet`() {
            serverTest {
                val (_, user1) = upsertUser()
                val (_, user2) = upsertUser()
                val (_, otherUser) = upsertUser()

                // Add another contact to see that only the specified one gets updated.
                contactMutation.addContact(context = user1!!.toAuthContext(), userId = otherUser!!.id)

                contactMutation.addContact(context = user1.toAuthContext(), userId = user2!!.id)
                contactMutation.addContact(context = user2.toAuthContext(), userId = user1.id)

                val actualContacts = getContacts().ignoreUnknown()
                val expectedContacts =
                    listOf(
                        Contact(userId_1 = user1.id, userId_2 = otherUser.id, isApproved = false),
                        Contact(userId_1 = user1.id, userId_2 = user2.id, isApproved = true)
                    ).ignoreUnknown()
                actualContacts shouldContainExactlyInAnyOrder expectedContacts
            }
        }
    }

    @Nested
    inner class RemoveContactTests {
        @Test
        fun `removes a contact`() {
            serverTest {
                val (_, user1) = upsertUser()
                val (_, user2) = upsertUser()
                val (_, otherUser) = upsertUser()

                val context = user1!!.toAuthContext()
                // Add another contact to see that only the specified one gets removed.
                contactMutation.addContact(context = context, userId = otherUser!!.id)
                contactMutation.addContact(context = context, userId = user2!!.id)

                contactMutation.removeContact(context = context, userId = user2.id)

                val actualContacts = getContacts().ignoreUnknown()
                val expectedContacts =
                    listOf(Contact(userId_1 = user1.id, userId_2 = otherUser.id, isApproved = false)).ignoreUnknown()
                actualContacts shouldContainExactlyInAnyOrder expectedContacts
            }
        }
    }
}
