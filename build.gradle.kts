import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    java
    `maven-publish`
}

group = "uk.co.sallery"
version = "0.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-java-jvm:2.1.0")
    implementation("io.ktor:ktor-client-core-jvm:2.1.0")
    implementation("io.ktor:ktor-network-jvm:2.1.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "uk.co.sallery"
            artifactId = "skydance"
            version = "0.0.1"

            from(components["java"])
        }
    }
}