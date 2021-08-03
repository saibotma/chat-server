package clientapi

import io.ktor.locations.*
import io.ktor.locations.put
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.routing.*
import java.util.*

@Location("/channels")
object ChannelList {
    @Location("/{channelId}")
    data class ChannelDetails(val channelId: UUID, val channelList: ChannelList) {
        @Location("/members")
        data class ChannelMemberList(val channelDetails: ChannelDetails) {
            @Location("/{userId}")
            data class ChannelMemberDetails(val userId: UUID, val channelMemberList: ChannelMemberList)
        }

        @Location("/messages")
        data class ChannelMessageList(val channelDetails: ChannelDetails) {
            @Location("/{messageId}")
            data class ChannelMessageDetails(val messageId: UUID, val channelMessageList: ChannelMessageList)
        }
    }
}

fun Route.installClientApi() {
    clientApiJwtAuthenticate {
        get<ChannelList> { TODO() }
        put<ChannelList.ChannelDetails> { TODO() }
        delete<ChannelList.ChannelDetails> { TODO() }

        put<ChannelList.ChannelDetails.ChannelMemberList.ChannelMemberDetails> { TODO() }
        delete<ChannelList.ChannelDetails.ChannelMemberList.ChannelMemberDetails> { TODO() }

        get<ChannelList.ChannelDetails.ChannelMessageList> { TODO() }
        put<ChannelList.ChannelDetails.ChannelMessageList.ChannelMessageDetails> { TODO() }
        delete<ChannelList.ChannelDetails.ChannelMessageList.ChannelMessageDetails> { TODO() }
    }
}
