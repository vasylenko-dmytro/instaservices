rootProject.name = "instaservices"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include("instagram-to-kafka-service")
include("app-config-data")
include("kafka")
include("kafka:kafka-admin")
include("kafka:kafka-model")
include("kafka:kafka-producer")
include("common-config")
include("config-server")