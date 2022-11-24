package testutil

import persistence.jooq.tables.pojos.Channel
import persistence.jooq.tables.pojos.ChannelMember
import persistence.jooq.tables.pojos.Contact

fun Channel.ignoreUnknown() = copy(id = null, createdAt = null, updatedAt = null)
fun ChannelMember.ignoreUnknown() = copy(addedAt = null, updatedAt = null)

fun Contact.ignoreUnknown() = copy(createdAt = null, updatedAt = null)

@JvmName("ignoreUnknownChannel")
fun Iterable<Channel>.ignoreUnknown() = map(Channel::ignoreUnknown)

@JvmName("ignoreUnknownChannelMember")
fun Iterable<ChannelMember>.ignoreUnknown() = map(ChannelMember::ignoreUnknown)

@JvmName("ignoreUnknownContact")
fun Iterable<Contact>.ignoreUnknown() = map(Contact::ignoreUnknown)
