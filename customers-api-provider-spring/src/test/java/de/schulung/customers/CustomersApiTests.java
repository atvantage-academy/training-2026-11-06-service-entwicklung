package de.schulung.customers;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomersApiTests {

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
   *       -> 201, mitgeschickte ID wird ignoriert, Server vergibt eine neue ID
   */
  @Test
  void given_customer_with_id_when_post_kunden_then_ignore_id() throws Exception {
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
   *       -> 201, unbekannte Attribute werden ignoriert und nicht zurückgegeben
   */
  @Test
  void given_customer_with_unknown_attributes_when_post_kunden_then_ignore_them() throws Exception {
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
