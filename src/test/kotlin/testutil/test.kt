package testutil

import org.flywaydb.core.Flyway

class TestRollbackException : Exception()

var didCleanUp = false

fun handleCleanUp(flyway: Flyway) {
    if (!didCleanUp) {
        restoreDatabase(flyway)
        didCleanUp = true
    }
}

fun restoreDatabase(flyway: Flyway) {
    flyway.clean()
    flyway.migrate()
}
