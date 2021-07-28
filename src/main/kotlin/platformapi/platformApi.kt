package platformapi

import io.ktor.locations.*
import io.ktor.routing.*
import java.util.*

@Location("/channels")
object ChannelList {
    @Location("/{channelId}")
    data class ChannelDetails(val channelId: UUID, val channelList: ChannelList) {
        @Location("/members")
        data class ChannelMemberList(val channelDetails: ChannelDetails) {
            @Location("/{userId}")
            data class ChannelMemberDetails(val userId: String, val channelMembers: ChannelMemberList) {

            }
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

}
