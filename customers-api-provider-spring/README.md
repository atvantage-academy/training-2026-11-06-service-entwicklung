# customers-api-provider-spring

This project uses Spring Boot.

If you want to learn more about Spring Boot, please visit its website: <https://spring.io/projects/spring-boot>.

## Creating the project

You can create the project again using the [Spring Initializr](https://start.spring.io/):

```shell script
curl https://start.spring.io/starter.zip \
    -d type=maven-project \
    -d bootVersion=4.1.2 \
    -d javaVersion=21 \
    -d groupId=de.schulung \
    -d artifactId=customers-api-provider-spring \
    -d packageName=de.schulung.customers \
    -d dependencies=web,devtools \
    -o customers-api-provider-spring.zip
```

## Running the application in dev mode

You can run your application using:

```shell script
mvn spring-boot:run
```

The application is available at <http://localhost:8080/>.

> **_NOTE:_**  The project includes the Spring Boot DevTools, which restart the application automatically whenever files on the classpath change (e.g. after recompiling in the IDE).

## Running the tests

You can run the tests using:

```shell script
mvn test
```

## Packaging and running the application

The application can be packaged using:

```shell script
mvn package
```

It produces the `customers-api-provider-spring-0.0.1-SNAPSHOT.jar` file in the `target/` directory.
It is an executable _über-jar_ that contains all dependencies.

The application is now runnable using `java -jar target/customers-api-provider-spring-0.0.1-SNAPSHOT.jar`.

## Building a container image

You can build an OCI container image (requires a running Docker daemon) using:

```shell script
mvn spring-boot:build-image
```

You can then run the container with: `docker run -p 8080:8080 customers-api-provider-spring:0.0.1-SNAPSHOT`

If you want to learn more about the Maven plugin, please consult <https://docs.spring.io/spring-boot/maven-plugin/>.

## Related Guides

- Spring Web ([guide](https://spring.io/guides/gs/rest-service/)): Build RESTful web services using Spring MVC
- Spring Boot DevTools ([reference](https://docs.spring.io/spring-boot/reference/using/devtools.html)): Fast application restarts and LiveReload during development
