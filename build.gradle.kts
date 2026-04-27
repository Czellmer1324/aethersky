allprojects {
    group = "com.czellmer1324"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    layout.buildDirectory.set(rootDir.resolve("output/${project.name}"))
}

repositories {
    mavenCentral()
}

tasks {

}