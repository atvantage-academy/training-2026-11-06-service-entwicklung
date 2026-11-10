package de.schulung;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class CustomersApiTests {

  // 1. GET /kunden -> 200
  // 2. Content-Type: application/json
  // 3. Objekt mit "items" als Array

  @Test
  void when_get_kunden_then_return_200() {
    given()
        .when().get("/kunden")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("items", is(notNullValue()))
      .body("items", is(instanceOf(List.class)));
  }

}
