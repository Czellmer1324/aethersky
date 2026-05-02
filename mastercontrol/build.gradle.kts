plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.czellmer1324"
version = "0.0.1-SNAPSHOT"
description = "mastercontrol"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":common"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val skipTests = providers.gradleProperty("skipTests").isPresent

tasks.withType<Test>().configureEach {
    enabled = !skipTests
}

tasks.register<Copy>("copy") {
    description = "moves jar to folder"
    dependsOn("build")

    from(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
    into("/Users/cody/Documents/mc-network/mastercontrol")
    rename { "app.jar" }
}

tasks.named("build") {
    finalizedBy("copy")
}
