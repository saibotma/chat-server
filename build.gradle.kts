import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.6.1"
val kotlinVersion = "1.5.30-M1"
val jooqVersion = "3.15.1"
val flywayCoreVersion = "7.11.4"
val kodeinVersion = "7.3.1"
val jacksonDataTypeJsr310Version = "2.12.4"
val kotestVersion = "4.6.1"
val junitJupiterVersion = "5.7.0"

plugins {
    application
    kotlin("jvm") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.flywaydb.flyway") version "7.11.4"
    id("nu.studer.jooq") version "6.0"
}

group = "dev.saibotma"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-stdlib", kotlinVersion)

    implementation("io.ktor", "ktor-server-netty", ktorVersion)
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor", "ktor-jackson", ktorVersion)
    implementation("io.ktor", "ktor-locations", ktorVersion)
    implementation("io.ktor", "ktor-server-test-host", ktorVersion)

    implementation("org.flywaydb", "flyway-core", flywayCoreVersion)
    implementation("org.jooq", "jooq", jooqVersion)

    implementation("org.kodein.di", "kodein-di-generic-jvm", kodeinVersion)
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

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
