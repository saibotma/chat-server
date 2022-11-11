package util

import io.ktor.server.config.*

val ApplicationConfig.serverPort: Int
    get() = property("server.port").getString().toInt()

val ApplicationConfig.postgresUser: String
    get() = property("postgres.user").getString()

val ApplicationConfig.postgresPassword: String
    get() = property("postgres.password").getString()

val ApplicationConfig.postgresServerName: String
    get() = property("postgres.serverName").getString()

val ApplicationConfig.postgresPort: Int
    get() = property("postgres.port").getString().toInt()

val ApplicationConfig.postgresDb: String
    get() = property("postgres.db").getString()

val ApplicationConfig.platformApiAccessToken: String
    get() = property("platformApi.accessToken").getString()

val ApplicationConfig.clientApiJwtSecret: String
    get() = property("clientApi.jwtSecret").getString()

val ApplicationConfig.firebaseCredentials: String?
    get() = propertyOrNull("firebase.credentials")?.getString()

val ApplicationConfig.flywayBaselineVersion: String
    get() = property("flyway.baselineVersion").getString()

val ApplicationConfig.flywayShouldBaseline: Boolean
    get() = property("flyway.shouldBaseline").getString().toBoolean()
