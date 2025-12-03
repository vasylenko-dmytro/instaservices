plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.vasylenko.edu"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven")
    }
}

dependencies {
    implementation(project(":app-config-data"))
    implementation(project(":kafka-consumer"))
    implementation(project(":kafka:kafka-admin"))
    implementation(project(":kafka:kafka-model"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.3.0")
    implementation("org.springframework.kafka:spring-kafka:3.3.11")
    implementation("io.confluent:kafka-avro-serializer:7.9.4")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    imageName = "${project.group}/kafka-to-elastic-service:${project.version}"
}