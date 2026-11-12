package de.schulung.customers;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class CustomersApiTests {

  private static final String MERGE_PATCH_JSON = "application/merge-patch+json";

  // 1. GET /kunden -> 200
  // 2. Content-Type: application/json
  // 3. Objekt mit "items" als Array

  @Autowired
  MockMvc mvc;

  @Test
  void when_get_kunden_then_return_200() throws Exception {
    mvc.perform(
        get("/kunden")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isMap())
      .andExpect(jsonPath("$.items").isArray());
  }

  // Test für die Content Negotiation
  // Accept: XML -> 406
  @Test
  void when_get_kunden_with_invalid_accept_then_return_406() throws Exception {
    mvc.perform(
        get("/kunden")
          .accept(MediaType.APPLICATION_XML)
      )
      .andExpect(status().isNotAcceptable());
  }

  /*
   *   - POST /kunden, Kunde im Body, Content-Type: application/json, Accept: application/json
   *       -> 201, Content-Type: application/json, Kunde im Body inkl. ID
   */
  @Test
  void given_valid_customer_when_post_kunden_then_return_201_and_customer_with_id() throws Exception {
    var result = mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Mayer",
                "birthdate": "1990-01-01",
                "status": "active"
              }
            """)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.name").value("Tom Mayer"))
      .andExpect(jsonPath("$.birthdate").value("1990-01-01"))
      .andExpect(jsonPath("$.status").value("active"))
      .andExpect(jsonPath("$.id", is(not(emptyOrNullString()))))
      .andReturn();

    String id = JsonPath.read(
      result.getResponse().getContentAsString(),
      "$.id"
    );

    header()
      .string("Location", "/kunden/" + id)
      .match(result);

  }

  /*
   *   - POST /kunden, Kunde im Body als XML, Content-Type: application/xml, Accept: application/json
   *       -> 415
   */
  @Test
  void given_customer_as_xml_when_post_kunden_then_return_415() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_XML)
          .content("""
              <kunde>
                <name>Tom Mayer</name>
                <birthdate>1990-01-01</birthdate>
                <status>active</status>
              </kunde>
            """)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isUnsupportedMediaType());
  }

  /*
   *   - POST /kunden, Kunde im Body, Content-Type: application/json, Accept: application/xml
   *       -> 406
   */
  @Test
  void given_valid_customer_when_post_kunden_with_invalid_accept_then_return_406() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Mayer",
                "birthdate": "1990-01-01",
                "status": "active"
              }
            """)
          .accept(MediaType.APPLICATION_XML)
      )
      .andExpect(status().isNotAcceptable());
  }

  /*
   *   - POST /kunden, Kunde im Body nicht vorhanden, Content-Type: application/json, Accept: application/json
   *       -> 400
   */
  @Test
  void given_no_customer_when_post_kunden_then_return_400() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - POST /kunden, Kunde im Body leer, Content-Type: application/json, Accept: application/json
   *       -> 400
   */
  @Test
  void given_empty_customer_when_post_kunden_then_return_400() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{}")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void given_customer_with_name_too_long_when_post_kunden_then_return_400() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "%s"
              }
            """.formatted("X".repeat(101)))
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - POST /kunden, Geburtsdatum heute oder morgen
   *       -> 400
   */
  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  void given_customer_with_birthdate_today_or_tomorrow_when_post_kunden_then_return_400(int daysFromToday) throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Mayer",
                "birthdate": "%s"
              }
            """.formatted(LocalDate.now().plusDays(daysFromToday)))
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - POST /kunden, Kunde im Body invalide (kein gültiges JSON), Content-Type: application/json, Accept: application/json
   *       -> 400
   */
  @Test
  void given_malformed_json_when_post_kunden_then_return_400() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Mayer",
            """)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - POST /kunden, Kunde im Body invalide (ungültiges Geburtsdatum), Content-Type: application/json, Accept: application/json
   *       -> 400
   */
  @Test
  void given_customer_with_invalid_birthdate_when_post_kunden_then_return_400() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Mayer",
                "birthdate": "gestern",
                "status": "active"
              }
            """)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - POST /kunden, Kunde im Body mit ID, Content-Type: application/json, Accept: application/json
   *       -> 400, eine ID darf nicht mitgeschickt werden
   */
  @Test
  void given_customer_with_id_when_post_kunden_then_return_400() throws Exception {
    var sentId = "00000000-0000-0000-0000-000000000001";
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "id": "%s",
                "name": "Tom Mayer",
                "birthdate": "1990-01-01",
                "status": "active"
              }
            """.formatted(sentId))
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - POST /kunden, Kunde im Body mit unbekannten Attributen, Content-Type: application/json, Accept: application/json
   *       -> 400, unbekannte Attribute sind nicht erlaubt
   */
  @Test
  void given_customer_with_unknown_attributes_when_post_kunden_then_return_400() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Mayer",
                "birthdate": "1990-01-01",
                "status": "active",
                "lieblingsfarbe": "blau"
              }
            """)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
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
  void given_customer_exists_when_get_customer_by_id_then_return_200_and_customer() throws Exception {

    // SETUP

    var result = mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Mayer",
                "birthdate": "1990-01-01",
                "status": "active"
              }
            """)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andReturn();

    String id = JsonPath.read(
      result.getResponse().getContentAsString(),
      "$.id"
    );

    // TEST

    mvc.perform(
        get("/kunden/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value(id))
      .andExpect(jsonPath("$.name").value("Tom Mayer"))
      .andExpect(jsonPath("$.birthdate").value("1990-01-01"))
      .andExpect(jsonPath("$.status").value("active"));

  }

  @Test
  void given_customer_does_not_exist_when_get_customer_by_id_then_return_404() throws Exception {

    String id = createKunde("""
        {
          "name": "Tom Mayer"
        }
      """);
    mvc.perform(delete("/kunden/{id}", id))
      .andExpect(status().isNoContent());

    mvc.perform(
        get("/kunden/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound());

  }

  @Test
  void given_invalid_id_when_get_customer_by_id_then_return_400() throws Exception {

    String id = "gelbekatze";

    mvc.perform(
        get("/kunden/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());

  }

  private String createKunde(String json) throws Exception {
    var result = mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andReturn();
    return JsonPath.read(
      result.getResponse().getContentAsString(),
      "$.id"
    );
  }

  private static String uniqueName() {
    return "Kunde " + UUID.randomUUID();
  }

  /*
   *   - POST /kunden, Kunde ohne Status
   *       -> 201, Kunde wird als aktiv angelegt
   */
  @Test
  void given_customer_without_status_when_post_kunden_then_status_is_active() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Mayer"
              }
            """)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value("active"));
  }

  /*
   *   - POST /kunden, Name länger als 100 Zeichen
   *       -> 400
   */
  @Test
  void given_customer_with_too_long_name_when_post_kunden_then_return_400() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "%s"
              }
            """.formatted("x".repeat(101)))
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - POST /kunden, unbekannter Status
   *       -> 400
   */
  @Test
  void given_customer_with_invalid_status_when_post_kunden_then_return_400() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Mayer",
                "status": "verschollen"
              }
            """)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - GET /kunden?name=..., Teil des Namens in anderer Schreibweise
   *       -> 200, nur Kunden, deren Name den Teil enthält
   */
  @Test
  void given_name_filter_when_get_kunden_then_return_matching_customers() throws Exception {
    var name = uniqueName();
    var id = createKunde("""
        {
          "name": "%s"
        }
      """.formatted(name));
    var teil = name.substring(6).toUpperCase();

    mvc.perform(
        get("/kunden")
          .queryParam("name", teil)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.items[*].id", hasItem(id)))
      .andExpect(jsonPath("$.items[*].name", everyItem(containsStringIgnoringCase(teil))));
  }

  /*
   *   - GET /kunden?state=locked
   *       -> 200, nur gesperrte Kunden
   */
  @Test
  void given_state_filter_when_get_kunden_then_return_matching_customers() throws Exception {
    var id = createKunde("""
        {
          "name": "Tom Mayer",
          "status": "locked"
        }
      """);

    mvc.perform(
        get("/kunden")
          .queryParam("state", "locked")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.items[*].id", hasItem(id)))
      .andExpect(jsonPath("$.items[*].status", everyItem(is(equalTo("locked")))));
  }

  /*
   *   - GET /kunden?name=...&state=locked
   *       -> 200, Filter werden UND-verknüpft
   */
  @Test
  void given_name_and_state_filter_when_get_kunden_then_combine_filters() throws Exception {
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

    mvc.perform(
        get("/kunden")
          .queryParam("name", name)
          .queryParam("state", "locked")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.items", hasSize(1)))
      .andExpect(jsonPath("$.items[0].id").value(id));
  }

  /*
   *   - GET /kunden?state=verschollen
   *       -> 400
   */
  @Test
  void given_invalid_state_filter_when_get_kunden_then_return_400() throws Exception {
    mvc.perform(
        get("/kunden")
          .queryParam("state", "verschollen")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - GET /kunden?name= (leer) bzw. länger als 100 Zeichen
   *       -> 400
   */
  @Test
  void given_empty_name_filter_when_get_kunden_then_return_400() throws Exception {
    mvc.perform(
        get("/kunden")
          .queryParam("name", "")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void given_too_long_name_filter_when_get_kunden_then_return_400() throws Exception {
    mvc.perform(
        get("/kunden")
          .queryParam("name", "x".repeat(101))
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - PUT /kunden/{id}, vollständiger Kunde
   *       -> 204, alle Daten ersetzt, nicht mitgeschickte optionale Felder entfernt
   */
  @Test
  void given_customer_exists_when_put_customer_then_return_204_and_replace_data() throws Exception {
    var id = createKunde("""
        {
          "name": "Tom Mayer",
          "birthdate": "1990-01-01",
          "status": "active"
        }
      """);

    mvc.perform(
        put("/kunden/{id}", id)
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Müller",
                "status": "disabled"
              }
            """)
      )
      .andExpect(status().isNoContent());

    mvc.perform(
        get("/kunden/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(id))
      .andExpect(jsonPath("$.name").value("Tom Müller"))
      .andExpect(jsonPath("$.birthdate", is(nullValue())))
      .andExpect(jsonPath("$.status").value("disabled"));
  }

  @Test
  void given_customer_does_not_exist_when_put_customer_then_return_404() throws Exception {
    mvc.perform(
        put("/kunden/{id}", UUID.randomUUID().toString())
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Mayer"
              }
            """)
      )
      .andExpect(status().isNotFound());
  }

  @Test
  void given_invalid_id_when_put_customer_then_return_400() throws Exception {
    mvc.perform(
        put("/kunden/{id}", "gelbekatze")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "Tom Mayer"
              }
            """)
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void given_customer_without_name_when_put_customer_then_return_400() throws Exception {
    var id = createKunde("""
        {
          "name": "Tom Mayer"
        }
      """);

    mvc.perform(
        put("/kunden/{id}", id)
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "birthdate": "1990-01-01"
              }
            """)
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void given_customer_with_id_when_put_customer_then_return_400() throws Exception {
    var id = createKunde("""
        {
          "name": "Tom Mayer"
        }
      """);

    mvc.perform(
        put("/kunden/{id}", id)
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "id": "%s",
                "name": "Tom Mayer"
              }
            """.formatted(id))
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - PATCH /kunden/{id}, { "status": "locked" }, Content-Type: application/merge-patch+json
   *       -> 204, nur der Status ist geändert
   */
  @Test
  void given_customer_exists_when_patch_status_then_return_204_and_change_only_status() throws Exception {
    var id = createKunde("""
        {
          "name": "Tom Mayer",
          "birthdate": "1990-01-01",
          "status": "active"
        }
      """);

    mvc.perform(
        patch("/kunden/{id}", id)
          .contentType(MERGE_PATCH_JSON)
          .content("""
              {
                "status": "locked"
              }
            """)
      )
      .andExpect(status().isNoContent());

    mvc.perform(
        get("/kunden/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("Tom Mayer"))
      .andExpect(jsonPath("$.birthdate").value("1990-01-01"))
      .andExpect(jsonPath("$.status").value("locked"));
  }

  /*
   *   - PATCH /kunden/{id}, { "birthdate": null }
   *       -> 204, Geburtsdatum ist entfernt
   */
  @Test
  void given_customer_exists_when_patch_birthdate_null_then_remove_birthdate() throws Exception {
    var id = createKunde("""
        {
          "name": "Tom Mayer",
          "birthdate": "1990-01-01"
        }
      """);

    mvc.perform(
        patch("/kunden/{id}", id)
          .contentType(MERGE_PATCH_JSON)
          .content("""
              {
                "birthdate": null
              }
            """)
      )
      .andExpect(status().isNoContent());

    mvc.perform(
        get("/kunden/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("Tom Mayer"))
      .andExpect(jsonPath("$.birthdate", is(nullValue())));
  }

  @Test
  void given_customer_does_not_exist_when_patch_customer_then_return_404() throws Exception {
    mvc.perform(
        patch("/kunden/{id}", UUID.randomUUID().toString())
          .contentType(MERGE_PATCH_JSON)
          .content("""
              {
                "status": "locked"
              }
            """)
      )
      .andExpect(status().isNotFound());
  }

  @Test
  void given_invalid_id_when_patch_customer_then_return_400() throws Exception {
    mvc.perform(
        patch("/kunden/{id}", "gelbekatze")
          .contentType(MERGE_PATCH_JSON)
          .content("""
              {
                "status": "locked"
              }
            """)
      )
      .andExpect(status().isBadRequest());
  }

  /*
   *   - PATCH /kunden/{id} mit ungültigem Inhalt (unbekannter Status, Name null, unbekanntes Feld)
   *       -> 400, Kunde bleibt unverändert
   */
  @Test
  void given_invalid_patch_when_patch_customer_then_return_400_and_keep_customer() throws Exception {
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
      mvc.perform(
          patch("/kunden/{id}", id)
            .contentType(MERGE_PATCH_JSON)
            .content(patch)
        )
        .andExpect(status().isBadRequest());
    }

    mvc.perform(
        get("/kunden/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("Tom Mayer"))
      .andExpect(jsonPath("$.status").value("active"));
  }

  /*
   *   - PATCH /kunden/{id}, Content-Type: application/json
   *       -> 415, nur JSON Merge Patch wird unterstützt
   */
  @Test
  void given_json_content_type_when_patch_customer_then_return_415() throws Exception {
    var id = createKunde("""
        {
          "name": "Tom Mayer"
        }
      """);

    mvc.perform(
        patch("/kunden/{id}", id)
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "status": "locked"
              }
            """)
      )
      .andExpect(status().isUnsupportedMediaType());
  }

  /*
   *   - DELETE /kunden/{id}
   *       -> 204, danach GET /kunden/{id} -> 404
   */
  @Test
  void given_customer_exists_when_delete_customer_then_return_204_and_customer_is_gone() throws Exception {
    var id = createKunde("""
        {
          "name": "Tom Mayer"
        }
      """);

    mvc.perform(delete("/kunden/{id}", id))
      .andExpect(status().isNoContent());

    mvc.perform(
        get("/kunden/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound());
  }

  @Test
  void given_customer_does_not_exist_when_delete_customer_then_return_404() throws Exception {
    mvc.perform(delete("/kunden/{id}", UUID.randomUUID().toString()))
      .andExpect(status().isNotFound());
  }

  @Test
  void given_invalid_id_when_delete_customer_then_return_400() throws Exception {
    mvc.perform(delete("/kunden/{id}", "gelbekatze"))
      .andExpect(status().isBadRequest());
  }

}
