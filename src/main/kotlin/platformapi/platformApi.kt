package platformapi

import clientapi.ClientApiConfig
import io.ktor.server.auth.*
import io.ktor.server.locations.*
import io.ktor.server.locations.post
import io.ktor.server.locations.put
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import persistence.jooq.KotlinDslContext
import platformapi.authentication.accesstoken.platformApiAccessTokenAuthentication
import java.util.*

@Location("/channels")
object ChannelList {
    @Location("/{channelId}")
    data class ChannelDetails(val channelId: UUID, val channelList: ChannelList) {
        @Location("/members")
        data class ChannelMemberList(val channelDetails: ChannelDetails) {
            @Location("/{userId}")
            data class ChannelMemberDetails(val userId: String, val channelMemberList: ChannelMemberList)
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

@Location("/contacts")
object ContactList {
    @Location("")
    data class ContactDetails(val userId1: String, val userId2: String)
}

fun Route.installPlatformApi() {
    authenticate(platformApiAccessTokenAuthentication) {
        val database: KotlinDslContext by closestDI().instance()
        val clientApiConfig: ClientApiConfig by closestDI().instance()

        post<ChannelList> { createChannel(it, database) }
        put<ChannelList.ChannelDetails> { updateChannel(it, database) }
        delete<ChannelList.ChannelDetails> { deleteChannel(it, database) }

        post<ChannelList.ChannelDetails.ChannelMemberList> { addMember(it, database) }
        put<ChannelList.ChannelDetails.ChannelMemberList> { updateMembers(it, database) }

        put<ChannelList.ChannelDetails.ChannelMemberList.ChannelMemberDetails> { updateMember(it, database) }
        delete<ChannelList.ChannelDetails.ChannelMemberList.ChannelMemberDetails> { deleteChannelMember(it, database) }

        put<UserList.UserDetails> { upsertUser(it, database) }
        delete<UserList.UserDetails> { deleteUser(it, database) }

        put<ContactList.ContactDetails> { upsertContact(it, database) }

        post<UserList.UserDetails.UserTokenList> { createUserToken(it, database, clientApiConfig.jwtSecret) }
    }
}
