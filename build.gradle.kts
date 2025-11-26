plugins {
	java
	id("org.springframework.boot") version "3.5.8"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.vasylenko.edu"
version = "0.0.1-SNAPSHOT"
description = "Microservices demo project"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {

	implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.kafka:spring-kafka:3.3.11")
    implementation("org.springframework.retry:spring-retry:2.0.12")

    implementation("org.apache.avro:avro:1.12.1")
    implementation("io.confluent:kafka-avro-serializer:7.9.4")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
}
