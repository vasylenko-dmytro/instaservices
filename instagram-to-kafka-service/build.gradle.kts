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
    implementation(project(":kafka:kafka-model"))
    implementation(project(":kafka:kafka-admin"))
    implementation(project(":kafka:kafka-producer"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.3.0")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:4.3.0")

    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    implementation("org.apache.avro:avro:1.12.1")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    imageName = "${project.group}/instagram-to-kafka-service:${project.version}"
}

tasks.test {
    useJUnitPlatform()
}