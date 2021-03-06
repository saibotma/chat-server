package util

import io.ktor.config.*

val HoconApplicationConfig.postgresUser: String
    get() = property("postgres.user").getString()

val HoconApplicationConfig.postgresPassword: String
    get() = property("postgres.password").getString()

val HoconApplicationConfig.postgresServerName: String
    get() = property("postgres.serverName").getString()

val HoconApplicationConfig.postgresPort: Int
    get() = property("postgres.port").getString().toInt()

val HoconApplicationConfig.postgresDb: String
    get() = property("postgres.db").getString()

val HoconApplicationConfig.platformApiAccessToken: String
    get() = property("platformApi.accessToken").getString()

val HoconApplicationConfig.clientApiJwtSecret: String
    get() = property("clientApi.jwtSecret").getString()

val HoconApplicationConfig.firebaseCredentials: String?
    get() = propertyOrNull("firebase.credentials")?.getString()

val HoconApplicationConfig.flywayBaselineVersion: String
    get() = property("flyway.baselineVersion").getString()

val HoconApplicationConfig.flywayShouldBaseline: Boolean
    get() = property("flyway.shouldBaseline").getString().toBoolean()
