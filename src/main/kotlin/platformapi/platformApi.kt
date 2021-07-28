package platformapi

import io.ktor.locations.*
import io.ktor.locations.put
import io.ktor.locations.post
import io.ktor.locations.delete
import io.ktor.routing.*
import java.nio.channels.Channel
import java.util.*

@Location("/channels")
object ChannelList {
    @Location("/{channelId}")
    data class ChannelDetails(val channelId: UUID, val channelList: ChannelList) {
        @Location("/members")
        data class ChannelMemberList(val channelDetails: ChannelDetails) {
            @Location("/{userId}")
            data class ChannelMemberDetails(val userId: String, val channelMembers: ChannelMemberList)
        }
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
        put<ChannelList.ChannelDetails> { TODO() }
        delete<ChannelList.ChannelDetails> { TODO() }

        put<ChannelList.ChannelDetails.ChannelMemberList> { TODO() }
        delete<ChannelList.ChannelDetails.ChannelMemberList> { TODO() }

        put<UserList.UserDetails> { TODO() }
        delete<UserList.UserDetails> { TODO() }

        post<UserList.UserDetails.UserTokenList> { TODO() }
    }
}
