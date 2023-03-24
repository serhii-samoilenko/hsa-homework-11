# Highload Software Architecture 8 Lesson 11 Homework

Index & Autocomplete
---

## Test project setup

The demo is written in Kotlin/Quarkus and uses docker-compose to run Elasticsearch.

The `com/example/Demo.kt/runDemo` function is used to set up index and run autocomplete queries. It also produces a report in a Markdown format. The final report is located in the [REPORT.md](reports/REPORT.md) file.

Please refer to the [REPORT.md](reports/REPORT.md) file for the information on mapping configuration and queries.

After launching the demo, you can open simple UI on [http://localhost:8080](http://localhost:8080) to try the queries yourself.

## How to build and run

Start up Elasticsearch

```shell script
docker-compose up -d
```

Build and run demo application (Requires Java 17+)

```shell script
./gradlew build && \
java -jar build/quarkus-app/quarkus-run.jar
```

You can also run application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```
