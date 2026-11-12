package de.schulung;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
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
        .body("$", instanceOf(Map.class))
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
   *   - POST /kunden, Kunde im Body, Content-Type: application/json, Accept: application/json
   *       -> 201, Content-Type: application/json, Kunde im Body inkl. ID
   */
  @Test
  void given_valid_customer_when_post_kunden_then_return_201_and_customer_with_id() {
    var response = given()
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
      .post("/kunden");

    response.then()
      .statusCode(201)
      .contentType(ContentType.JSON)
      .body("name", is(equalTo("Tom Mayer")))
      .body("birthdate", is(equalTo("1990-01-01")))
      .body("status", is(equalTo("active")))
      .body("id", is(not(emptyOrNullString())));

    String id = response
      .jsonPath()
      .getString("id");

    response.then()
      .header("Location", is(equalTo("/kunden/" + id)));

  }

  /*
   *   - POST /kunden, Kunde im Body als XML, Content-Type: application/xml, Accept: application/json
   *       -> 415
   */
  @Test
  void given_customer_as_xml_when_post_kunden_then_return_415() {
    given()
      .contentType(ContentType.XML)
      .body("""
        <kunde>
          <name>Tom Mayer</name>
          <birthdate>1990-01-01</birthdate>
          <status>active</status>
        </kunde>
      """)
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(415);
  }

  /*
   *   - POST /kunden, Kunde im Body, Content-Type: application/json, Accept: application/xml
   *       -> 406
   */
  @Test
  void given_valid_customer_when_post_kunden_with_invalid_accept_then_return_406() {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "Tom Mayer",
          "birthdate": "1990-01-01",
          "status": "active"
        }
      """)
      .accept(ContentType.XML)
      .when()
      .post("/kunden")
      .then()
      .statusCode(406);
  }

  /*
   *   - POST /kunden, Kunde im Body nicht vorhanden, Content-Type: application/json, Accept: application/json
   *       -> 400
   */
  @Test
  void given_no_customer_when_post_kunden_then_return_400() {
    given()
      .contentType(ContentType.JSON)
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(400);
  }

  /*
   *   - POST /kunden, Kunde im Body leer, Content-Type: application/json, Accept: application/json
   *       -> 400
   */
  @Test
  void given_empty_customer_when_post_kunden_then_return_400() {
    given()
      .contentType(ContentType.JSON)
      .body("{}")
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(400);
  }

  /*
   *   - POST /kunden, Kunde im Body invalide (kein gültiges JSON), Content-Type: application/json, Accept: application/json
   *       -> 400
   */
  @Test
  void given_malformed_json_when_post_kunden_then_return_400() {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "Tom Mayer",
      """)
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(400);
  }

  /*
   *   - POST /kunden, Kunde im Body invalide (ungültiges Geburtsdatum), Content-Type: application/json, Accept: application/json
   *       -> 400
   */
  @Test
  void given_customer_with_invalid_birthdate_when_post_kunden_then_return_400() {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "Tom Mayer",
          "birthdate": "gestern",
          "status": "active"
        }
      """)
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(400);
  }

  /*
   *   - POST /kunden, Kunde im Body mit ID, Content-Type: application/json, Accept: application/json
   *       -> 400, eine ID darf nicht mitgeschickt werden
   */
  @Test
  void given_customer_with_id_when_post_kunden_then_return_400() {
    var sentId = "00000000-0000-0000-0000-000000000001";
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "id": "%s",
          "name": "Tom Mayer",
          "birthdate": "1990-01-01",
          "status": "active"
        }
      """.formatted(sentId))
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(400);
  }

  /*
   *   - POST /kunden, Kunde im Body mit unbekannten Attributen, Content-Type: application/json, Accept: application/json
   *       -> 400, unbekannte Attribute sind nicht erlaubt
   */
  @Test
  void given_customer_with_unknown_attributes_when_post_kunden_then_return_400() {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "Tom Mayer",
          "birthdate": "1990-01-01",
          "status": "active",
          "lieblingsfarbe": "blau"
        }
      """)
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(400);
  }

  // TODO: GET /kunden/{id}
  /*
   * Positivfall:
   *   - POST /kunden -> 201, ID auslesen
   *   - GET /kunden/{id}, id ist ID der vorherigen Anfrage, Accept: application/json
   *      -> 200, Kunde mit korrekten Attributen im Body
   *  Negativfälle:
   *   - GET /kunden/{id} mit Zufalls-UUID, Accept: application/json
   *      -> 404 (später, komplizierter: Anlegen, Löschen, Abfragen)
   *   - GET /kunden/{id} mit syntaktisch falscher ID (keine UUID)
   *      -> 400
   */

}
