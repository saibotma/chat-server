import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val postgresUser = System.getenv("POSTGRES_USER") ?: "postgres"
val postgresPassword = System.getenv("POSTGRES_PASSWORD") ?: "postgres"
val postgresServerName = System.getenv("POSTGRES_SERVERNAME") ?: "localhost"
val postgresPort = System.getenv("POSTGRES_PORT") ?: "54324"
val postgresDb = System.getenv("POSTGRES_DB") ?: "chat-server"
val postgresUrl = "jdbc:postgresql://$postgresServerName:$postgresPort/$postgresDb"

val ktorVersion = "1.6.1"
val kotlinVersion = "1.5.21"
val kotlinxCoroutinesVersion = "1.5.1"
val postgreSqlJdbcVersion = "42.2.23"
val log4jVersion = "2.14.1"
val log4jApiKotlinVersion = "1.0.0"
val graphQlJavaVersion = "16.2"
val graphQlKotlinVersion = "4.1.1"
val firebaseAdminVersion = "6.9.0"
val jooqVersion = "3.15.1"
val flywayCoreVersion = "7.11.4"
val kodeinVersion = "7.6.0"
val jacksonDataTypeJsr310Version = "2.12.4"
val kotestVersion = "4.6.1"
val junitJupiterVersion = "5.7.2"
val mockkVersion = "1.12.0"

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")

plugins {
    application
    kotlin("jvm") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.flywaydb.flyway") version "7.11.4"
    id("nu.studer.jooq") version "6.0"
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

group = "dev.saibotma"
version = "0.1.3"

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/kodein-framework/Kodein-DI/") }
}

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-stdlib", kotlinVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", kotlinxCoroutinesVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-reactive", kotlinxCoroutinesVersion)

    implementation("io.ktor", "ktor-server-netty", ktorVersion)
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor", "ktor-jackson", ktorVersion)
    implementation("io.ktor", "ktor-locations", ktorVersion)
    implementation("io.ktor", "ktor-server-test-host", ktorVersion)
    implementation("io.ktor", "ktor-auth-jwt", ktorVersion)

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

    testImplementation(kotlin("test-junit5"))
    testImplementation("io.kotest", "kotest-assertions-core", kotestVersion)
    testImplementation("io.kotest", "kotest-property", kotestVersion)
    testImplementation("io.ktor", "ktor-server-tests", ktorVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitJupiterVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", junitJupiterVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitJupiterVersion)
    testImplementation("io.mockk", "mockk", mockkVersion)
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
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

configurations {
    // We need to remove the logback-classic package
    // To avoid: Multiple bindings were found on the class path
    // From: http://www.slf4j.org/codes.html#multiple_bindings
    runtime.get().exclude("ch.qos.logback", "logback-classic")
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf("Main-Class" to application.mainClassName))
    }
    isZip64 = true
    archiveFileName.set("chat-server.jar")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

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



abstract class PrintVersionTask : DefaultTask() {
    @TaskAction
    fun printVersion() {
        println(project.version)
    }
}

// Create a task using the task type
tasks.register<PrintVersionTask>("printVersion")

