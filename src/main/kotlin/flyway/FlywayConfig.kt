package flyway

data class FlywayConfig(val baselineVersion: String, val shouldBaseline: Boolean)
