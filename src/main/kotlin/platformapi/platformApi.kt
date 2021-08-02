package platformapi

import io.ktor.locations.*
import io.ktor.locations.put
import io.ktor.locations.post
import io.ktor.locations.delete
import io.ktor.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import persistence.jooq.KotlinDslContext
import java.nio.channels.Channel
import java.util.*

@Location("/channels")
object ChannelList {
    @Location("/{channelId}")
    data class ChannelDetails(val channelId: UUID, val channelList: ChannelList) {
        @Location("/meta")
        data class ChannelMetaDetails(val channelDetails: ChannelDetails)

        @Location("/members")
        data class ChannelMemberList(val channelDetails: ChannelDetails)
    }
}

@Location("/users")
object UserList {
    @Location("/{userId}")
    data class UserDetails(val userId: String, val userList: UserList) {
        @Location("/tokens")
        data class UserTokenList(val userDetails: UserDetails)
    }
}

fun Route.installPlatformApi() {
    platformApiAccessTokenAuthenticate {
        val database: KotlinDslContext by closestDI().instance()
        put<ChannelList.ChannelDetails> { upsertChannel(it, database) }
        delete<ChannelList.ChannelDetails> { deleteChannel(it, database) }

        put<ChannelList.ChannelDetails.ChannelMetaDetails> { updateChannelMeta(it, database) }
        put<ChannelList.ChannelDetails.ChannelMemberList> { updateMembers(it, database) }

        put<UserList.UserDetails> { TODO() }
        delete<UserList.UserDetails> { TODO() }

        post<UserList.UserDetails.UserTokenList> { TODO() }
    }
}
