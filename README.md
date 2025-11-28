# InstaServices (instagram-to-kafka)

Short description
-----------------
A lightweight Java Spring Boot multi-module project that reads Instagram data (by configured keywords/user) and publishes events to a local Kafka cluster. The repository includes a Config Server, Kafka cluster Docker Compose definitions (Zookeeper, Kafka broker, Schema Registry), and an instagram-to-kafka service that runs as a background stream producer to Kafka.

Software & hardware requirements
-------------------------------
- Docker Desktop (tested on Docker Engine + Docker Compose v2)
- Java 17+ (only required if building locally; Docker images run Java inside container)
- Gradle (optional — use the included `gradlew`/`gradlew.bat`)
- Recommended: 16 GB RAM (local Kafka + Schema Registry require memory)

Bring up the application (development) using Docker Compose
---------------------------------------------------------
This repo includes Docker Compose files under the `docker-compose/` directory which bring up a local Kafka cluster and the instagram-to-kafka service.

1. From repository root, change to the `docker-compose` folder and bring up the stack:

```powershell
cd docker-compose
# Compose is composed via .env -> COMPOSE_FILE (common.yml:kafka_cluster.yml:services.yml)
docker compose up -d
```

2. Verify containers are running:

```powershell
docker compose ps
```

3. Helpful commands:

```powershell
# Stream logs for a specific service
docker compose logs -f instagram-to-kafka-service

# Stop and remove
docker compose down
```

What the Compose files start
---------------------------
- Zookeeper — exposed on localhost:2181
- Kafka broker — exposed on localhost:9092
- Schema Registry — exposed on localhost:8081
- instagram-to-kafka-service — container exposes port 5005 (JVM debug). The service is a background stream application (no HTTP API exposed by default).
- Config Server (not included in compose) — runs as Spring Cloud Config server and listens on port 8888 when started locally via `./gradlew :config-server:bootRun` or the IDE.

Key files used by Compose
-------------------------
- `docker-compose/.env` — compose file list and versions
- `docker-compose/common.yml`
- `docker-compose/kafka_cluster.yml` — zookeeper, kafka-broker, schema-registry definitions and ports
- `docker-compose/services.yml` — instagram-to-kafka-service image, env, volumes

Environment variables and config
-------------------------------
- `docker-compose/.env` sets `SERVICE_VERSION`, `KAFKA_VERSION`, `GROUP_ID`, and `GLOBAL_NETWORK` used by compose
- `instagram-to-kafka-service` expects the following environment variables (wired via compose `env_file` and `environment`):
  - `ACCESS_TOKEN` (Instagram access token) — set in `.env` if you want the container to pick it up
  - `IG_USER_ID` (Instagram user id)
  - `KAFKA_CONFIG_BOOTSTRAP_SERVERS` (defaults to `kafka-broker-1:9092` inside compose network)
  - `KAFKA_CONFIG_SCHEMA_REGISTRY_URL` (defaults to `http://schema-registry:8081`)

Testing the product (browser & API)
-----------------------------------
This project is primarily an ingest pipeline to Kafka. There is no web UI provided in the repository. Useful endpoints and UI points to test:

- Schema Registry UI: http://localhost:8081
- Kafka broker (no UI) at `localhost:9092` — use Kafka CLI tools or tools like `kafkacat` / `kafka-console-consumer` to inspect topics
- Config Server (when run locally via Gradle/IDE): http://localhost:8888 — will serve configuration from `config-server-repository`

Example commands
----------------
- Consume from the topic `ig-topic` (requires Kafka CLI container or tools):

```powershell
# Example using kafka-console-consumer inside a container (adjust versions if needed)
docker run --rm -it --network application confluentinc/cp-kafka:7.6.0 \
  kafka-console-consumer --bootstrap-server kafka-broker-1:9092 --topic ig-topic --from-beginning
```

- Check Schema Registry subjects:

```powershell
curl http://localhost:8081/subjects
```

Credentials & placeholders
-------------------------
- No credentials are configured by default for Kafka, Zookeeper, or Schema Registry in the provided compose files.
- Instagram access token and user id must be provided by you (set `ACCESS_TOKEN` and `IG_USER_ID` in `docker-compose/.env` or a `.env` at repo root). Example placeholder values (replace before use):
  - ACCESS_TOKEN=your_instagram_access_token_here
  - IG_USER_ID=your_instagram_user_id_here

Where to look for endpoints and configuration
--------------------------------------------
- Config server sources: `config-server-repository/` (contains client property yml files)
- service config: `instagram-to-kafka-service/src/main/resources/application.yaml`
- kafka config classes: `kafka/kafka-producer`, `kafka/kafka-admin`, `kafka/kafka-model`

Notes & assumptions
-------------------
- The `instagram-to-kafka-service` runs as a non-HTTP background process (it implements `CommandLineRunner` and starts streaming to Kafka). The only exposed port in compose is `5005` for remote debugging.
- Config Server is configured to read from the local `config-server-repository` git folder (see `config-server/src/main/resources/bootstrap.yml`) and listens on port `8888` when run.
- If you need the instagram service to expose HTTP health endpoints or REST APIs, add a `server.port` property to `application.yaml` and map it in `docker-compose/services.yml`.

Manual scan and store hashtag IDs (Instagram Graph API)
-----------------------------------------------------
Purpose

This project contains a helper component `HashtagIdsUpdater` (class: `instagram-to-kafka-service/src/main/java/com/vasylenko/edu/ig/to/kafka/service/util/HashtagIdsUpdater.java`) that can resolve human-readable Instagram hashtags into their numeric Graph API IDs using the Instagram Graph API (`ig_hashtag_search`) and write the results into a properties file.

Why you might run this
- The service may prefer to work with hashtag IDs when querying the IG Graph API. Pre-resolving hashtag IDs avoids extra lookups at runtime.

Prerequisites
- A valid Instagram (Facebook) Graph API access token with rights to query `ig_hashtag_search`.
- The Instagram user id used for the token (IG_USER_ID).
- These values should be provided as environment variables: `ACCESS_TOKEN` and `IG_USER_ID`.

Where the results are stored
- The component writes a properties-style file named `hashtag-ids.properties` to the repository path:

  instagram-to-kafka-service/src/main/resources/hashtag-ids.properties

  Each line has the format: `hashtag=id` (for example: `docker=1784156XXXXX`).

Note: when running inside Docker the working directory and file paths may differ; for a simple developer workflow run the updater locally (see instructions below) so the file is written into the project tree.

How to run the updater

Option A — (Quick, automatic) Enable the updater in code and run the service
1. Open `HashtagIdsUpdater.java` and in the `run(ApplicationArguments args)` method uncomment the call to `updateHashtagIds();` so it executes on application startup.
2. Provide your credentials in your environment or a `.env` file (example `.env` entries):

```powershell
$env:ACCESS_TOKEN = "your_instagram_access_token_here"
$env:IG_USER_ID = "your_instagram_user_id_here"
```

3. Start the application locally (Gradle wrapper on Windows):

```powershell
# from repository root
.\gradlew.bat :instagram-to-kafka-service:bootRun
```

4. The updater will attempt to resolve hashtags configured in `application.yaml` and write `hashtag-ids.properties` to `instagram-to-kafka-service/src/main/resources/`.

Option B — (Manual, no code change) Query the IG Graph API directly and create the file yourself

1. Use curl to query the hashtag search endpoint for each hashtag (replace placeholders):

```powershell
# Example for hashtag 'docker'
$ACCESS_TOKEN = "your_instagram_access_token_here"
$IG_USER_ID = "your_instagram_user_id_here"
$HASHTAG = "docker"

curl "https://graph.facebook.com/v24.0/ig_hashtag_search?user_id=$IG_USER_ID&q=$HASHTAG&access_token=$ACCESS_TOKEN"
```

2. The response will contain a JSON object with a `data` array. The first element's `id` is the hashtag id. Example response snippet:

```json
{
  "data": [
    { "id": "1784156..." }
  ]
}
```

3. Create `instagram-to-kafka-service/src/main/resources/hashtag-ids.properties` and add lines for each hashtag you resolved:

```
docker=1784156...
aws=17891....
springboot=17902....
```

Tips and notes
- The `HashtagIdsUpdater` code uses `Dotenv` to read `.env` values when launching from the project. If you use the automatic option, ensure the `.env` file or environment variables are available to the process.
- The updater is intentionally not enabled by default (the call is commented out). This avoids accidental API calls during normal startup and gives control to the developer.
- The Graph API has rate limits. If you plan to resolve many hashtags, add delays or use the updater in small batches.
- If you want the updater to run inside the Docker container, adjust the output path or add a volume mapping so the generated file is persisted to the host.

Troubleshooting
- If you receive errors about permissions or an invalid token, confirm the `ACCESS_TOKEN` scope and the `IG_USER_ID` belong to the same business account.
- For debugging, you can run the updater method in the IDE and inspect logs printed by `HashtagIdsUpdater`.

Next steps / Improvements
------------------------
- Add a small health/metrics HTTP endpoint to `instagram-to-kafka-service` (e.g., actuator) and expose it in `services.yml` for easier health checks.
- Add an admin UI (e.g., Kafka Manager or Kowl) to the compose stack for easier topic inspection.

Contact / Testing credentials
-----------------------------
There are no default credentials; please supply Instagram API credentials and Kafka tools of choice.

---
Generated on: Nov 27, 2025
