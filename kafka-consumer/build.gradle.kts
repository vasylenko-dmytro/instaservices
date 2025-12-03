plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.vasylenko.edu"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app-config-data"))
    implementation("org.springframework.kafka:spring-kafka:3.3.11")
    implementation("org.apache.avro:avro:1.12.1")
}