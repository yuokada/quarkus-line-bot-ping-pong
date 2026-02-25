# quarkus-line-bot-ping-pong

A LINE Messaging API webhook handler built with [Quarkus](https://quarkus.io/), deployable as a standalone server or as an AWS Lambda function via AWS SAM.

Receives LINE webhook events and replies to each message with the original text appended with " pong!".

## Requirements

- Java 25
- Maven (via `./mvnw` wrapper)
- AWS CLI + AWS SAM CLI (for Lambda deployment)

## Running in dev mode

Live reload via Quarkus Dev Services:

```shell
./mvnw compile quarkus:dev
```

The Dev UI is available at http://localhost:8080/q/dev/ (dev mode only).

## Configuration

Set LINE credentials in `src/main/resources/application.properties` or via environment variables:

```
LINE_BOT_CHANNEL_SECRET=<your-secret>
LINE_BOT_CHANNEL_TOKEN=<your-token>
```

Environment variables take precedence over `application.properties` (Quarkus convention: dots → underscores, uppercase).

## Building

**JVM build (SAM profile, for Lambda):**

```shell
./mvnw -Psam package
# or
make build
```

**Native build (requires Docker):**

```shell
make NATIVE_BUILD=true build
# equivalent to:
./mvnw package -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker
```

## Testing

**Run all tests:**

```shell
./mvnw test
```

**Run a single test class:**

```shell
./mvnw test -Dtest=GreetingTest
```

**Run a single test method:**

```shell
./mvnw test -Dtest=GreetingTest#testCallbackAdd
```

## Deploying to AWS Lambda (JVM)

The SAM template (`sam.jvm.yaml`) defines a Java 25 Lambda function (512 MB, 15s timeout) with an HTTP API Gateway trigger. Deployment targets region `ap-northeast-1`, stack `quarkus-line-ping-pong`.

```shell
make deploy_jvm
```

This copies `sam.jvm.yaml` and `samconfig.toml` into `target/`, then runs `sam deploy`.

## Health check

MicroProfile liveness endpoint: `GET /q/health/live`

## Related Guides

- AWS Lambda ([guide](https://quarkus.io/guides/amazon-lambda)): Write AWS
  Lambda functions
- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): A
  JAX-RS implementation utilizing build time processing and Vert.x. This
  extension is not compatible with the quarkus-resteasy extension, or any of the
  extensions that depend on it.
- AWS Lambda Gateway REST API
  ([guide](https://quarkus.io/guides/amazon-lambda-http)): Build an API Gateway
  REST API with Lambda integration
