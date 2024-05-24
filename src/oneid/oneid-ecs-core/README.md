# One Identity

---

## Summary 📖

- [Api Documentation 📖 [TODO]](#api-documentation-todo)
- [Technology Stack 📚](#technology-stack-)
- [Start Project Locally 🚀](#start-project-locally-)
    * [Running the application in dev mode](#running-the-application-in-dev-mode)
    * [Run locally with Docker](#run-locally-with-docker)
- [Develop Locally 💻](#develop-locally-)
    * [Prerequisites](#prerequisites)
    * [Testing 🧪 [TODO]](#testing-todo)
        + [Unit test [TODO]](#unit-test-todo)
        + [Integration test [TODO]](#integration-test-todo)
        + [Performance test [TODO]](#performance-test-todo)

---

## Api Documentation 📖 [TODO]

---

## Technology Stack 📚

- Java 21 Runtime Environment GraalVM CE
- [Quarkus](https://quarkus.io/)

---

## Start Project Locally 🚀

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev -P oneid-ecs-core-aggregate
```

> **_NOTE:_**  This command must be executed from **oneid** directory.

### Run locally with Docker

`docker build -f oneid-ecs-core/Dockerfile -t local/oneid-ecs-core .`

Then

`docker run -i --rm -p 8080:8080 local/oneid-ecs-core`

## Develop Locally 💻

### Prerequisites

- git
- maven (v3.9.6)
- jdk-21

### Testing 🧪[TODO]

#### Unit test [TODO]

#### Integration test [TODO]

#### Performance test [TODO]
