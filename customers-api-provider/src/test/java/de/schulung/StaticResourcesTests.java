package de.schulung;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;


@QuarkusTest
class StaticResourcesTests {

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
