# Message Application

The Spring Boot application can be described by the following diagram:

![Diagram](application-diagram.png)

## Setting up AWS resources for the application

Initializing the stack using Gradle and Pulumi:

```shell
./gradlew initStack
```

Creating the stack:

```shell
./gradlew createStack
```

Destroying the stack:

```shell
./gradlew destroyStack
```

## Environment

Make sure you have Java 17+ and a [compatible Docker environment](https://www.testcontainers.org/supported_docker_environment/) installed.

## Running the app

```shell
./gradlew runSpringBootApp
```

## Running Tests

```shell
./gradlew test
```
