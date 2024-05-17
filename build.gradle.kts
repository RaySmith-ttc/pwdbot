import org.jetbrains.kotlin.cli.jvm.main

plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "ru.raysmith"
version = "1.0"

val env = System.getenv("env")?.toUpperCase() ?: "DEV"

repositories {
    mavenLocal()
    mavenCentral()
    mavenRaySmith("utils")
    mavenRaySmith("tg-bot")
    mavenRaySmith("exposed-option")
}

dependencies {
    implementation("ru.raysmith:tg-bot:1.0.0-alpha.2")
    implementation("ru.raysmith:utils:1.4.1")
//    implementation("ru.raysmith:exposed-option:1.0")
    
    val exposedVersion = "0.40.1"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("mysql:mysql-connector-java:8.0.32")
    
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("com.zaxxer:HikariCP:5.0.1")
    
    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(8)
}

sourceSets {
    main {
        resources {
            val dir = "${project.projectDir}/src/main"
            srcDirs(File("$dir/resources-${env.lowercase()}"))
            srcDirs(File("$dir/shared-resources"))
        }
    }
}

application {
    mainClass.set("MainKt")
}

fun RepositoryHandler.mavenRaySmith(name: String) {
    maven {
        url = uri("https://maven.pkg.github.com/raysmith-ttc/$name")
        credentials {
            username = System.getenv("GIT_USERNAME")
            password = System.getenv("GIT_TOKEN_READ")
        }
    }
}