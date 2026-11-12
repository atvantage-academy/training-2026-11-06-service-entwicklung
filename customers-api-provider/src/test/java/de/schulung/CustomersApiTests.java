package de.schulung;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
@TestTransaction
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

  @Test
  void given_customer_with_name_too_long_when_post_kunden_then_return_400() {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "%s"
        }
        """.formatted("X".repeat(101)))
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(400);
  }

  /*
   *   - POST /kunden, Geburtsdatum heute oder morgen
   *       -> 400
   */
  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  void given_customer_with_birthdate_today_or_tomorrow_when_post_kunden_then_return_400(int daysFromToday) {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "Tom Mayer",
          "birthdate": "%s"
        }
        """.formatted(LocalDate.now().plusDays(daysFromToday)))
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

  @Test
  void given_customer_exists_when_get_customer_by_id_then_return_200_and_customer() {

    // SETUP

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
      .statusCode(201);

    String id = response
      .jsonPath()
      .getString("id");

    // TEST

    given()
      .accept(ContentType.JSON)
      .pathParam("id", id)
      .when()
      .get("/kunden/{id}")
      .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("id", is(equalTo(id)))
      .body("name", is(equalTo("Tom Mayer")))
      .body("birthdate", is(equalTo("1990-01-01")))
      .body("status", is(equalTo("active")));

  }

  @Test
  void given_customer_does_not_exist_when_get_customer_by_id_then_return_404() {

    String id = createKunde("""
        {
          "name": "Tom Mayer"
        }
      """);
    given()
      .pathParam("id", id)
      .when()
      .delete("/kunden/{id}")
      .then()
      .statusCode(204);

    given()
      .accept(ContentType.JSON)
      .pathParam("id", id)
      .when()
      .get("/kunden/{id}")
      .then()
      .statusCode(404);

  }

  @Test
  void given_invalid_id_when_get_customer_by_id_then_return_400() {

    String id = "gelbekatze";

    given()
      .accept(ContentType.JSON)
      .pathParam("id", id)
      .when()
      .get("/kunden/{id}")
      .then()
      .statusCode(400);

  }

  private static String createKunde(String json) {
    return given()
      .contentType(ContentType.JSON)
      .body(json)
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(201)
      .extract()
      .jsonPath()
      .getString("id");
  }

  private static String uniqueName() {
    return "Kunde " + UUID.randomUUID();
  }

  /*
   *   - POST /kunden, Kunde ohne Status
   *       -> 201, Kunde wird als aktiv angelegt
   */
  @Test
  void given_customer_without_status_when_post_kunden_then_status_is_active() {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "Tom Mayer"
        }
      """)
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(201)
      .body("status", is(equalTo("active")));
  }

  /*
   *   - POST /kunden, Name länger als 100 Zeichen
   *       -> 400
   */
  @Test
  void given_customer_with_too_long_name_when_post_kunden_then_return_400() {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "%s"
        }
      """.formatted("x".repeat(101)))
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(400);
  }

  /*
   *   - POST /kunden, unbekannter Status
   *       -> 400
   */
  @Test
  void given_customer_with_invalid_status_when_post_kunden_then_return_400() {
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "name": "Tom Mayer",
          "status": "verschollen"
        }
      """)
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(400);
  }

  /*
   *   - GET /kunden?name=..., Teil des Namens in anderer Schreibweise
   *       -> 200, nur Kunden, deren Name den Teil enthält
   */
  @Test
  void given_name_filter_when_get_kunden_then_return_matching_customers() {
    var name = uniqueName();
    var id = createKunde("""
        {
          "name": "%s"
        }
      """.formatted(name));
    var teil = name.substring(6).toUpperCase();

    given()
      .accept(ContentType.JSON)
      .queryParam("name", teil)
      .when()
      .get("/kunden")
      .then()
      .statusCode(200)
      .body("items.id", hasItem(id))
      .body("items.name", everyItem(containsStringIgnoringCase(teil)));
  }

  /*
   *   - GET /kunden?state=locked
   *       -> 200, nur gesperrte Kunden
   */
  @Test
  void given_state_filter_when_get_kunden_then_return_matching_customers() {
    var id = createKunde("""
        {
          "name": "Tom Mayer",
          "status": "locked"
        }
      """);

    given()
      .accept(ContentType.JSON)
      .queryParam("state", "locked")
      .when()
      .get("/kunden")
      .then()
      .statusCode(200)
      .body("items.id", hasItem(id))
      .body("items.status", everyItem(is(equalTo("locked"))));
  }

  /*
   *   - GET /kunden?name=...&state=locked
   *       -> 200, Filter werden UND-verknüpft
   */
  @Test
  void given_name_and_state_filter_when_get_kunden_then_combine_filters() {
    var name = uniqueName();
    createKunde("""
        {
          "name": "%s",
          "status": "active"
        }
      """.formatted(name));
    var id = createKunde("""
        {
          "name": "%s",
          "status": "locked"
        }
      """.formatted(name));

    given()
      .accept(ContentType.JSON)
      .queryParam("name", name)
      .queryParam("state", "locked")
      .when()
      .get("/kunden")
      .then()
      .statusCode(200)
      .body("items", hasSize(1))
      .body("items[0].id", is(equalTo(id)));
  }

  /*
   *   - GET /kunden?state=verschollen
   *       -> 400
   */
  @Test
  void given_invalid_state_filter_when_get_kunden_then_return_400() {
    given()
      .accept(ContentType.JSON)
      .queryParam("state", "verschollen")
      .when()
      .get("/kunden")
      .then()
      .statusCode(400);
  }

  /*
   *   - GET /kunden?name= (leer) bzw. länger als 100 Zeichen
   *       -> 400
   */
  @Test
  void given_empty_name_filter_when_get_kunden_then_return_400() {
    given()
      .accept(ContentType.JSON)
      .queryParam("name", "")
      .when()
      .get("/kunden")
      .then()
      .statusCode(400);
  }

  @Test
  void given_too_long_name_filter_when_get_kunden_then_return_400() {
    given()
      .accept(ContentType.JSON)
      .queryParam("name", "x".repeat(101))
      .when()
      .get("/kunden")
      .then()
      .statusCode(400);
  }

  /*
   *   - PUT /kunden/{id}, vollständiger Kunde
   *       -> 204, alle Daten ersetzt, nicht mitgeschickte optionale Felder entfernt
   */
  @Test
  void given_customer_exists_when_put_customer_then_return_204_and_replace_data() {
    var id = createKunde("""
        {
          "name": "Tom Mayer",
          "birthdate": "1990-01-01",
          "status": "active"
        }
      """);

    given()
      .contentType(ContentType.JSON)
      .pathParam("id", id)
      .body("""
        {
          "name": "Tom Müller",
          "status": "disabled"
        }
      """)
      .when()
      .put("/kunden/{id}")
      .then()
      .statusCode(204);

    given()
      .accept(ContentType.JSON)
      .pathParam("id", id)
      .when()
      .get("/kunden/{id}")
      .then()
      .statusCode(200)
      .body("id", is(equalTo(id)))
      .body("name", is(equalTo("Tom Müller")))
      .body("birthdate", is(nullValue()))
      .body("status", is(equalTo("disabled")));
  }

  @Test
  void given_customer_does_not_exist_when_put_customer_then_return_404() {
    given()
      .contentType(ContentType.JSON)
      .pathParam("id", UUID.randomUUID().toString())
      .body("""
        {
          "name": "Tom Mayer"
        }
      """)
      .when()
      .put("/kunden/{id}")
      .then()
      .statusCode(404);
  }

  @Test
  void given_invalid_id_when_put_customer_then_return_400() {
    given()
      .contentType(ContentType.JSON)
      .pathParam("id", "gelbekatze")
      .body("""
        {
          "name": "Tom Mayer"
        }
      """)
      .when()
      .put("/kunden/{id}")
      .then()
      .statusCode(400);
  }

  @Test
  void given_customer_without_name_when_put_customer_then_return_400() {
    var id = createKunde("""
        {
          "name": "Tom Mayer"
        }
      """);

    given()
      .contentType(ContentType.JSON)
      .pathParam("id", id)
      .body("""
        {
          "birthdate": "1990-01-01"
        }
      """)
      .when()
      .put("/kunden/{id}")
      .then()
      .statusCode(400);
  }

  @Test
  void given_customer_with_id_when_put_customer_then_return_400() {
    var id = createKunde("""
        {
          "name": "Tom Mayer"
        }
      """);

    given()
      .contentType(ContentType.JSON)
      .pathParam("id", id)
      .body("""
        {
          "id": "%s",
          "name": "Tom Mayer"
        }
      """.formatted(id))
      .when()
      .put("/kunden/{id}")
      .then()
      .statusCode(400);
  }

  /*
   *   - PATCH /kunden/{id}, { "status": "locked" }, Content-Type: application/merge-patch+json
   *       -> 204, nur der Status ist geändert
   */
  @Test
  void given_customer_exists_when_patch_status_then_return_204_and_change_only_status() {
    var id = createKunde("""
        {
          "name": "Tom Mayer",
          "birthdate": "1990-01-01",
          "status": "active"
        }
      """);

    given()
      .contentType("application/merge-patch+json")
      .pathParam("id", id)
      .body("""
        {
          "status": "locked"
        }
      """)
      .when()
      .patch("/kunden/{id}")
      .then()
      .statusCode(204);

    given()
      .accept(ContentType.JSON)
      .pathParam("id", id)
      .when()
      .get("/kunden/{id}")
      .then()
      .statusCode(200)
      .body("name", is(equalTo("Tom Mayer")))
      .body("birthdate", is(equalTo("1990-01-01")))
      .body("status", is(equalTo("locked")));
  }

  /*
   *   - PATCH /kunden/{id}, { "birthdate": null }
   *       -> 204, Geburtsdatum ist entfernt
   */
  @Test
  void given_customer_exists_when_patch_birthdate_null_then_remove_birthdate() {
    var id = createKunde("""
        {
          "name": "Tom Mayer",
          "birthdate": "1990-01-01"
        }
      """);

    given()
      .contentType("application/merge-patch+json")
      .pathParam("id", id)
      .body("""
        {
          "birthdate": null
        }
      """)
      .when()
      .patch("/kunden/{id}")
      .then()
      .statusCode(204);

    given()
      .accept(ContentType.JSON)
      .pathParam("id", id)
      .when()
      .get("/kunden/{id}")
      .then()
      .statusCode(200)
      .body("name", is(equalTo("Tom Mayer")))
      .body("birthdate", is(nullValue()));
  }

  @Test
  void given_customer_does_not_exist_when_patch_customer_then_return_404() {
    given()
      .contentType("application/merge-patch+json")
      .pathParam("id", UUID.randomUUID().toString())
      .body("""
        {
          "status": "locked"
        }
      """)
      .when()
      .patch("/kunden/{id}")
      .then()
      .statusCode(404);
  }

  @Test
  void given_invalid_id_when_patch_customer_then_return_400() {
    given()
      .contentType("application/merge-patch+json")
      .pathParam("id", "gelbekatze")
      .body("""
        {
          "status": "locked"
        }
      """)
      .when()
      .patch("/kunden/{id}")
      .then()
      .statusCode(400);
  }

  /*
   *   - PATCH /kunden/{id} mit ungültigem Inhalt (unbekannter Status, Name null, unbekanntes Feld)
   *       -> 400, Kunde bleibt unverändert
   */
  @Test
  void given_invalid_patch_when_patch_customer_then_return_400_and_keep_customer() {
    var id = createKunde("""
        {
          "name": "Tom Mayer",
          "status": "active"
        }
      """);

    for (var patch : List.of(
      """
        { "name": "Tom Müller", "status": "verschollen" }
      """,
      """
        { "name": null }
      """,
      """
        { "lieblingsfarbe": "blau" }
      """
    )) {
      given()
        .contentType("application/merge-patch+json")
        .pathParam("id", id)
        .body(patch)
        .when()
        .patch("/kunden/{id}")
        .then()
        .statusCode(400);
    }

    given()
      .accept(ContentType.JSON)
      .pathParam("id", id)
      .when()
      .get("/kunden/{id}")
      .then()
      .statusCode(200)
      .body("name", is(equalTo("Tom Mayer")))
      .body("status", is(equalTo("active")));
  }

  /*
   *   - PATCH /kunden/{id}, Content-Type: application/json
   *       -> 415, nur JSON Merge Patch wird unterstützt
   */
  @Test
  void given_json_content_type_when_patch_customer_then_return_415() {
    var id = createKunde("""
        {
          "name": "Tom Mayer"
        }
      """);

    given()
      .contentType(ContentType.JSON)
      .pathParam("id", id)
      .body("""
        {
          "status": "locked"
        }
      """)
      .when()
      .patch("/kunden/{id}")
      .then()
      .statusCode(415);
  }

  /*
   *   - DELETE /kunden/{id}
   *       -> 204, danach GET /kunden/{id} -> 404
   */
  @Test
  void given_customer_exists_when_delete_customer_then_return_204_and_customer_is_gone() {
    var id = createKunde("""
        {
          "name": "Tom Mayer"
        }
      """);

    given()
      .pathParam("id", id)
      .when()
      .delete("/kunden/{id}")
      .then()
      .statusCode(204);

    given()
      .accept(ContentType.JSON)
      .pathParam("id", id)
      .when()
      .get("/kunden/{id}")
      .then()
      .statusCode(404);
  }

  @Test
  void given_customer_does_not_exist_when_delete_customer_then_return_404() {
    given()
      .pathParam("id", UUID.randomUUID().toString())
      .when()
      .delete("/kunden/{id}")
      .then()
      .statusCode(404);
  }

  @Test
  void given_invalid_id_when_delete_customer_then_return_400() {
    given()
      .pathParam("id", "gelbekatze")
      .when()
      .delete("/kunden/{id}")
      .then()
      .statusCode(400);
  }

}
