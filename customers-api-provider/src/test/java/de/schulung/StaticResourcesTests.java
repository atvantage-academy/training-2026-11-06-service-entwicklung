package de.schulung;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class StaticResourcesTests {

  @Test
  void when_get_index_then_return_index_html() {
    given()
      .when()
        .get("/index.html")
      .then()
        .statusCode(200)
        .contentType("text/html");
  }

}
