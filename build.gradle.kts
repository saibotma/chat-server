val postgresUser = System.getenv("POSTGRES_USER") ?: "postgres"
val postgresPassword = System.getenv("POSTGRES_PASSWORD") ?: "postgres"
val postgresServerName = System.getenv("POSTGRES_SERVERNAME") ?: "localhost"
val postgresPort = System.getenv("POSTGRES_PORT") ?: "54324"
val postgresDb = System.getenv("POSTGRES_DB") ?: "chat-server"
val postgresUrl = "jdbc:postgresql://$postgresServerName:$postgresPort/$postgresDb"

val kotlinVersion = "1.7.21"
val ktorVersion = "2.1.3"
val log4jVersion = "2.19.0"
val log4jApiKotlinVersion = "1.2.0"
val graphQlKotlinVersion = "6.3.0"
val firebaseAdminVersion = "9.1.1"
val flywayCoreVersion = "9.7.0"
val postgreSqlJdbcVersion = "42.5.0"
val jooqVersion = "3.17.4"
// TODO(saibotma): Remove me when https://github.com/Kodein-Framework/Kodein-DI/issues/410 is resolved.
val kodeinVersion = "8.0.0-ktor-2-SNAPSHOT"
val jacksonDataTypeJsr310Version = "2.14.0"
val kotestVersion = "5.5.4"
val junitJupiterVersion = "5.9.1"
val mockkVersion = "1.13.2"

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")

plugins {
    application
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.flywaydb.flyway") version "9.7.0"
    id("nu.studer.jooq") version "8.0"
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

group = "dev.saibotma"
version = "0.1.2"

repositories {
    mavenCentral()
    // TODO(saibotma): Remove me when https://github.com/Kodein-Framework/Kodein-DI/issues/410 is resolved.
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-stdlib", kotlinVersion)

    implementation("io.ktor", "ktor-server-netty", ktorVersion)
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-data-conversion:$ktorVersion")
    implementation("io.ktor:ktor-server-locations:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")
    implementation("io.ktor:ktor-server-double-receive:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-test-host:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    // Out of some reason the plugin returns 403 in case CORS would not be allowed. Related issues:
    // https://youtrack.jetbrains.com/issue/KTOR-4237/CORS-the-plugin-responds-with-403-although-specification-doesnt-contain-such-information
    // https://youtrack.jetbrains.com/issue/KTOR-4236/CORS-Plugin-should-log-reason-for-returning-403-Forbidden-errors
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor", "ktor-client-core", ktorVersion)
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets-jvm:2.1.3")
    testImplementation("io.ktor", "ktor-server-tests", ktorVersion)


    implementation("org.apache.logging.log4j", "log4j-api", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-api-kotlin", log4jApiKotlinVersion)

    implementation("com.expediagroup", "graphql-kotlin-schema-generator", graphQlKotlinVersion)
    implementation("com.expediagroup", "graphql-kotlin-server", graphQlKotlinVersion)

    implementation("com.google.firebase", "firebase-admin", firebaseAdminVersion)

    implementation("org.flywaydb", "flyway-core", flywayCoreVersion)
    implementation("org.postgresql", "postgresql", postgreSqlJdbcVersion)
    jooqGenerator("org.postgresql", "postgresql", postgreSqlJdbcVersion)
    implementation("org.jooq", "jooq", jooqVersion)

    runtimeOnly("org.kodein.di", "kodein-di-jvm", kodeinVersion)
    implementation("org.kodein.di", "kodein-di-framework-ktor-server-jvm", kodeinVersion)

    implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", jacksonDataTypeJsr310Version)

    testImplementation("io.kotest", "kotest-assertions-core", kotestVersion)
    testImplementation("io.kotest", "kotest-property", kotestVersion)

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitJupiterVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", junitJupiterVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitJupiterVersion)

    testImplementation("io.mockk", "mockk", mockkVersion)

    // TODO(saibotma): Remove me.
    implementation("io.projectreactor:reactor-core:3.5.0")
    // TODO(saibotma): Add version variable.
    implementation("org.postgresql:r2dbc-postgresql:1.0.0.RELEASE")
}

tasks.test {
    useJUnitPlatform()
}

// Copy and pasted this from https://github.com/etiennestuder/gradle-jooq-plugin#gradle-kotlin-dsl-4
// with some small adjustments.
jooq {
    version.set(jooqVersion)

    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = postgresUrl
                    user = postgresUser
                    password = postgresPassword
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        includes = ".*"

                        forcedTypes.addAll(
                            arrayOf(
                                org.jooq.meta.jaxb.ForcedType()
                                    .withUserType("java.time.Instant")
                                    .withConverter("persistence.jooq.InstantConverter")
                                    // "\\s*" stands for multiple spaces
                                    .withIncludeTypes("timestamp\\s*with\\s*time\\s*zone")
                            )
                        )
                    }
                    generate.apply {
                        // Don't generate deprecated code
                        isDeprecated = false
                        isImmutablePojos = true
                        // Needed because else Arrays don't equal by content
                        isPojosEqualsAndHashCode = true
                    }
                    target.apply {
                        packageName = "persistence.jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

flyway {
    url = postgresUrl
    user = postgresUser
    password = postgresPassword
    // The default is true, and Flyway gradle plugin will not be executed in production environment.
    cleanDisabled = false
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

configurations.implementation {
    // We need to remove the logback-classic package
    // To avoid: Multiple bindings were found on the class path
    // From: http://www.slf4j.org/codes.html#multiple_bindings
    exclude("ch.qos.logback", "logback-classic")
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf("Main-Class" to application.mainClassName))
    }
    isZip64 = true
    archiveFileName.set("chat-server.jar")
}

tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
    dependsOn(tasks.flywayMigrate)
    inputs.files(fileTree("src/main/resources/db/migration"))
        .withPropertyName("migration")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    allInputsDeclared.set(true)
    outputs.cacheIf { true }
}

// Required because of https://github.com/gradle/gradle/issues/17236
tasks.named<Copy>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

abstract class PrintVersionTask : DefaultTask() {
    @TaskAction
    fun printVersion() {
        println(project.version)
    }
}

// Create a task using the task type
tasks.register<PrintVersionTask>("printVersion")

