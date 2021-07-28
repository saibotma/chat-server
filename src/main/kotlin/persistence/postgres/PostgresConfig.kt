package persistence.postgres

data class PostgresConfig(
    val user: String,
    val password: String,
    val serverName: String,
    val port: Int,
    val db: String
)
