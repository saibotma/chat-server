package testutil

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container

open class DatabaseTest {
    @Container
    val postgres = PostgreSQLContainer<Nothing>("postgres:11.5-alpine")
}
