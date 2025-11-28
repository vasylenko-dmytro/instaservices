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
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:4.3.0")
    implementation("org.springframework.cloud:spring-cloud-config-server:4.3.0")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    imageName = "${project.group}/config-server:${project.version}"
}