plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "2.2.20"
    idea
    // Apply the application plugin to add support for building a CLI application.
    application
    id("com.gradleup.shadow") version "9.2.2"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")

}



dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // eclipse lsp implementation
    implementation("org.eclipse.lsp4j", "org.eclipse.lsp4j", "0.24.0")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-test-junit5
    testImplementation(kotlin("test-junit5"))

    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.14.0")

    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-params
    testImplementation("org.junit.jupiter", "junit-jupiter-params", "5.14.0")

    implementation(platform("de.hhu.stups:prob-java-bom:4.15.0"))
    implementation("de.hhu.stups", "prob-java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

application {
    mainClass.set("b.language.server.AppKt") // The main class of the application
}

