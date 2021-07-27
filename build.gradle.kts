import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.Companion.attribute
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val postgresUser = System.getenv("POSTGRES_USER") ?: "postgres"
val postgresPassword = System.getenv("POSTGRES_PASSWORD") ?: "postgres"
val postgresServerName = System.getenv("POSTGRES_SERVERNAME") ?: "localhost"
val postgresPort = System.getenv("POSTGRES_PORT") ?: "54324"
val postgresDb = System.getenv("POSTGRES_DB") ?: "chat-server"
val postgresUrl = "jdbc:postgresql://$postgresServerName:$postgresPort/$postgresDb"

val ktorVersion = "1.4.1"
val kotlinVersion = "1.5.30-M1"
val postgreSqlJdbcVersion = "42.2.20"
val postgreSqlR2dbcVersion = "0.8.8.RELEASE"
val log4jVersion = "2.12.1"
val log4jApiKotlinVersion = "1.0.0"
val jooqVersion = "3.15.1"
val flywayCoreVersion = "6.5.5"
val kodeinVersion = "6.3.3"
val jacksonDataTypeJsr310Version = "2.11.3"
val kotestVersion = "4.3.1"
val junitJupiterVersion = "5.7.0"

plugins {
    application
    kotlin("jvm") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("org.flywaydb.flyway") version "6.5.5"
    id("nu.studer.jooq") version "5.2"
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

group = "dev.saibotma"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/kodein-framework/Kodein-DI/") }
}

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-stdlib", kotlinVersion)

    implementation("io.ktor", "ktor-server-netty", ktorVersion)
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor", "ktor-jackson", ktorVersion)
    implementation("io.ktor", "ktor-locations", ktorVersion)
    implementation("io.ktor", "ktor-server-test-host", ktorVersion)

    implementation("org.apache.logging.log4j", "log4j-api", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-api-kotlin", log4jApiKotlinVersion)

    implementation("org.flywaydb", "flyway-core", flywayCoreVersion)
    implementation("io.r2dbc", "r2dbc-postgresql", postgreSqlR2dbcVersion)
    implementation("org.postgresql", "postgresql", postgreSqlJdbcVersion)
    jooqGenerator("org.postgresql", "postgresql", postgreSqlJdbcVersion)
    implementation("org.jooq", "jooq", jooqVersion)

    implementation("org.kodein.di", "kodein-di-generic-jvm", kodeinVersion)
    //implementation("org.kodein.di", "kodein-di-framework-ktor-server-jvm", kodeinVersion)
    implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", jacksonDataTypeJsr310Version)

    testImplementation(kotlin("test-junit5"))
    testImplementation("io.kotest", "kotest-assertions-core", kotestVersion)
    testImplementation("io.kotest", "kotest-property", kotestVersion)
    testImplementation("io.ktor", "ktor-server-tests", ktorVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitJupiterVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitJupiterVersion)
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
                                    .withConverter("app.appella.persistence.jooq.InstantConverter")
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
                        packageName = "dev.saibotma.persistence.postgres.jooq"
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
    baselineOnMigrate = true
    baselineVersion = "1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf("Main-Class" to application.mainClassName))
    }
    isZip64 = true
    archiveFileName.set("chat-server.jar")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"

    // Disable Kotlin experimental warnings
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xuse-experimental=kotlin.Experimental",
        "-Xuse-experimental=io.ktor.locations.KtorExperimentalLocationsAPI",
        "-Xuse-experimental=kotlin.ExperimentalStdlibApi"
    )
}

tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
    dependsOn(tasks.flywayMigrate)
    inputs.files(fileTree("src/main/resources/db/migration"))
        .withPropertyName("migration")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    allInputsDeclared.set(true)
    outputs.cacheIf { true }
}
