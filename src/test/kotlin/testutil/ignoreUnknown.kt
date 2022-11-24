package testutil

import persistence.jooq.tables.pojos.Channel
import persistence.jooq.tables.pojos.ChannelMember

fun Channel.ignoreUnknown() = copy(id = null, createdAt = null, updatedAt = null)
fun ChannelMember.ignoreUnknown() = copy(addedAt = null, updatedAt = null)

@JvmName("ignoreUnknownChannel")
fun Iterable<Channel>.ignoreUnknown() = map(Channel::ignoreUnknown)

@JvmName("ignoreUnknownChannelMember")
fun Iterable<ChannelMember>.ignoreUnknown() = map(ChannelMember::ignoreUnknown)
