val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

val coroutinesVersion = "1.5.2"
val openApiGeneratorVersion = "0.2-beta.20"
val jacksonDatatypeVersion = "2.9.8"
val swaggerUiVersion = "3.25.0"
val reflectionsVersion = "0.9.11"
val bcryptVersion = "0.9.0"
val postgresqlVersion = "42.2.12"
val exposedVersion = "0.17.13"
val hikariCpVersion = "2.7.8"
val flywayVersion = "9.22.3"
val retrofitVersion = "2.3.0"
val okhttpVersion = "3.10.0"
val junitVersion = "5.7.1"
val assertjVersion = "3.19.0"
val restAssuredVersion = "4.3.3"
val hamcrestVersion = "2.2"

plugins {
    application
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "mobi.sevenwinds"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("mobi.sevenwinds.ApplicationKt")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")
    implementation("io.ktor:ktor-metrics:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("com.github.papsign:Ktor-OpenAPI-Generator:$openApiGeneratorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonDatatypeVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonDatatypeVersion")
    implementation("org.webjars:swagger-ui:$swaggerUiVersion")
    implementation("org.reflections:reflections:$reflectionsVersion")

    implementation("at.favre.lib:bcrypt:$bcryptVersion")

    implementation("org.postgresql:postgresql:$postgresqlVersion")

    implementation("org.jetbrains.exposed:exposed:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikariCpVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-jackson:$retrofitVersion")
    implementation("com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("org.hamcrest:hamcrest:$hamcrestVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xjvm-default=all")
    }
}