
plugins {
    kotlin("jvm") version  "2.3.21" 
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.run-velocity") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("io.lettuce:lettuce-core:7.5.1.RELEASE")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-velocity-api:2.22.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-velocity-core:2.22.0")
}

kotlin {
    jvmToolchain(25)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

  runVelocity {
    // Configure the Velocity version for our task.
    // This is the only required configuration besides applying the plugin.
    // Your plugin's jar (or shadowJar if present) will be used automatically.
    velocityVersion("3.1.1")
  }

    processResources {
        val props = mapOf("version" to version )
        filesMatching("velocity-plugin.json") {
            expand(props)
        }
    }
}
