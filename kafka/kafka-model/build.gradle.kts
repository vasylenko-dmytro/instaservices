plugins {
    java
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

group = "com.vasylenko.edu"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.avro:avro:1.12.1")
}

avro {
    fieldVisibility = "PUBLIC"
}