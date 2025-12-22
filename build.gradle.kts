plugins {
    kotlin("jvm") version "2.1.0-Beta2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
    id("xyz.jpenilla.run-paper") version "2.3.1" // Paper server for testing/hotloading JVM
}

group = "net.mvndicraft"
version = "1.0.0-SNAPSHOT"
description = "A plugin template in kotlin."
java.sourceCompatibility = JavaVersion.VERSION_21
val mainMinecraftVersion = "1.21.1"
val townyVersion = "0.101.0.2"
val siegeWarVersion = "3.1.0"
val mvndiRemote = repositories.maven("https://repo.mvndicraft.net/repository/maven-snapshots/") {
    name = "Mvndi"
    credentials {
        username = project.findProperty("mvndi.user") as String? ?: System.getenv("MVNDI_MVN_USER")
        password = project.findProperty("mvndi.key") as String? ?: System.getenv("MVNDI_MVN_KEY")
    }
}

repositories {
    mavenLocal()
    mavenCentral()

    // Paper
    maven("https://repo.papermc.io/repository/maven-public/")

    // Kotlin
    maven("https://oss.sonatype.org/content/groups/public/")

    maven("https://jitpack.io")

    maven("https://repo.minebench.de/")

    // Mvndi
    mvndiRemote
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$mainMinecraftVersion-R0.1-SNAPSHOT")
    compileOnly("com.github.TownyAdvanced:SiegeWar:2.19.3")
    compileOnly("net.mvndicraft:mvndicore:2.0.0-SNAPSHOT")
    compileOnly("net.mvndicraft:mvndibattle:2.0.1-SNAPSHOT")
    implementation("net.jodah:expiringmap:0.5.11")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("fr.formiko.mc.biomeutils:biomeutils:1.1.8")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    processResources {
        val props = mapOf(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description,
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    runServer {
        downloadPlugins {
            github(
                "TownyAdvanced",
                "Towny",
                "$townyVersion",
                "towny-$townyVersion.jar"
            )
            github(
                "TownyAdvanced",
                "SiegeWar",
                "$siegeWarVersion",
                "SiegeWar-$siegeWarVersion.jar"
            )
        }
        minecraftVersion("$mainMinecraftVersion")
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    repositories {
        mvndiRemote
    }
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        artifactId = "mvndisoundscapes"
    }
}
