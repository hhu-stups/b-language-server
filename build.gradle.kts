plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "1.9.22"
    idea
    // Apply the application plugin to add support for building a CLI application.
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")

}



dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // eclipse lsp implementation
    implementation("org.eclipse.lsp4j", "org.eclipse.lsp4j", "0.20.1")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-test-junit5
    testImplementation(kotlin("test-junit5"))

    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.10.2")

    implementation(platform("de.hhu.stups:prob-java-bom:4.13.0"))
    implementation("de.hhu.stups", "prob-java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>(){
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("b.language.server.AppKt") // The main class of the application
}

