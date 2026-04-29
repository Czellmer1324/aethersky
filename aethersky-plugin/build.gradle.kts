plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven ("https://repo.infernalsuite.com/repository/maven-snapshots/")
}

val ktorVersion = "3.4.3"

dependencies {
    implementation(project(":common"))
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("com.infernalsuite.asp:api:4.0.0-SNAPSHOT")
    implementation("io.ktor:ktor-client-core:${ktorVersion}")
    implementation("io.ktor:ktor-client-okhttp:${ktorVersion}")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.22.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.22.0")
}

kotlin {
    jvmToolchain(25)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.11")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version )
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

tasks.register<Copy>("copyToServer") {
    description = "Moves jar to plugin folder"
    from(layout.buildDirectory.file("libs/${project.name}-${project.version}-all.jar"))
    into(file("/Users/cody/AetherSkyServer/plugins")) // Change this to your server's path

    // Optional: Rename the file during the move
    rename { "aethersky.jar" }

    // Ensure the build runs before copying
    dependsOn("shadowJar") // Use "jar" if not using the Shadow plugin
}
