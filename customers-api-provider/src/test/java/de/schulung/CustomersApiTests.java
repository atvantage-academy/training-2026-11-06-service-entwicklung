package de.schulung;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class CustomersApiTests {

  // 1. GET /kunden -> 200
  // 2. Content-Type: application/json
  // 3. Objekt mit "items" als Array

  @Test
  void when_get_kunden_then_return_200() {
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/kunden")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("items", is(notNullValue()))
      .body("items", is(instanceOf(List.class)));
  }

  // Test für die Content Negotiation
  // Accept: XML -> 406
  @Test
  void when_get_kunden_with_invalid_accept_then_return_406() {
    given()
      .accept(ContentType.XML)
      .when()
      .get("/kunden")
      .then()
      .statusCode(406);
  }

  /*
   * POST /kunden
   * - Positivfall:
   *   - POST /kunden, Kunde im Body, Content-Type: application/json, Accept: application/json
   *       -> 201, Content-Type: application/json, Kunde im Body inkl. ID
   * - Fehlerfälle:
   *   - POST /kunden, Kunde im Body als XML, Content-Type: application/xml, Accept: application/json
   *   - POST /kunden, Kunde im Body leer/invalide/nicht vorhanden, Content-Type: application/json, Accept: application/json
   *   - POST /kunden, Kunde im Body mit mehr Attributen (insb. mit ID), Content-Type: application/json, Accept: application/json
   *   - POST /kunden, Kunde im Body, Content-Type: application/json, Accept: application/xml
   * - Zusatz:
   *   - Location-Header?
   */

  /*
   *   - POST /kunden, Kunde im Body, Content-Type: application/json, Accept: application/json
   *       -> 201, Content-Type: application/json, Kunde im Body inkl. ID
   */

  @Test
  void given_valid_customer_when_post_kunden_then_return_201_and_customer_with_id() {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "Tom Mayer",
          "birthdate": "1990-01-01",
          "status": "active"
        }
      """)
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(201)
      .contentType(ContentType.JSON)
      .body("name", is(equalTo("Tom Mayer")))
      .body("birthdate", is(equalTo("1990-01-01")))
      .body("status", is(equalTo("active")))
      .body("id", is(not(emptyOrNullString())));
  }

}
