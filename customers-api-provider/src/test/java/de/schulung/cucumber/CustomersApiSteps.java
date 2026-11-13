package de.schulung.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import io.quarkiverse.cucumber.ScenarioScope;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/*
 * Übersetzt die fachlichen Schritte der Feature-Dateien in Aufrufe der REST-API.
 *
 * Platzhalter in Attributwerten und erwarteten Werten:
 *   {id}                 ID des zuletzt angelegten Kunden
 *   {eindeutig}          pro Szenario eindeutiger Text, z. B. für Namen
 *   {EINDEUTIG}          derselbe Text in Großbuchstaben
 *   {heute}, {morgen}    Datum im Format JJJJ-MM-TT
 *   {Text mit N Zeichen} Text der Länge N
 *
 * Attribute werden mit ihrem deutschen Namen angegeben (Name/Namen, Geburtsdatum,
 * Status, ID); alle anderen werden kleingeschrieben als JSON-Feld übernommen.
 */
@ScenarioScope
public class CustomersApiSteps {

  private static final String MERGE_PATCH_JSON = "application/merge-patch+json";
  private static final Pattern TEXT_MIT_LAENGE = Pattern.compile("\\{Text mit (\\d+) Zeichen}");
  private static final Map<String, String> FELDNAMEN = Map.of(
    "Name", "name",
    "Namen", "name",
    "Geburtsdatum", "birthdate",
    "Status", "status",
    "ID", "id"
  );
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final String eindeutig = UUID.randomUUID().toString();
  private String id;
  private final Map<String, Object> kundendaten = new LinkedHashMap<>();
  private final Map<String, Object> aenderung = new LinkedHashMap<>();
  private Response response;

  private String ersetzePlatzhalter(String text) {
    var ergebnis = text
      .replace("{eindeutig}", eindeutig)
      .replace("{EINDEUTIG}", eindeutig.toUpperCase())
      .replace("{heute}", LocalDate.now().toString())
      .replace("{morgen}", LocalDate.now().plusDays(1).toString());
    if (id != null) {
      ergebnis = ergebnis.replace("{id}", id);
    }
    Matcher matcher = TEXT_MIT_LAENGE.matcher(ergebnis);
    return matcher.replaceAll(treffer -> "x".repeat(Integer.parseInt(treffer.group(1))));
  }

  private static String feldname(String attribut) {
    return FELDNAMEN.getOrDefault(attribut, attribut.toLowerCase());
  }

  private static String alsJson(Map<String, Object> daten) {
    try {
      return OBJECT_MAPPER.writeValueAsString(daten);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String alsXml(Map<String, Object> daten) {
    var xml = new StringBuilder("<kunde>");
    daten.forEach((feld, wert) -> xml.append("<%1$s>%2$s</%1$s>".formatted(feld, wert)));
    return xml.append("</kunde>").toString();
  }

  private void legeKundeAn(String name, String geburtsdatum, String status) {
    var daten = new LinkedHashMap<String, Object>();
    daten.put("name", ersetzePlatzhalter(name));
    if (geburtsdatum != null) {
      daten.put("birthdate", ersetzePlatzhalter(geburtsdatum));
    }
    if (status != null) {
      daten.put("status", status);
    }
    id = given()
      .contentType(ContentType.JSON)
      .body(alsJson(daten))
      .accept(ContentType.JSON)
      .when()
      .post("/kunden")
      .then()
      .statusCode(201)
      .extract()
      .jsonPath()
      .getString("id");
  }

  private static RequestSpecification anfrage() {
    return given().accept(ContentType.JSON);
  }

  private static RequestSpecification anfrageMitJson(String json) {
    return anfrage()
      .contentType(ContentType.JSON)
      .body(json);
  }

  private void sucheKunden(Map<String, String> filter) {
    var anfrage = anfrage();
    filter.forEach((name, wert) -> anfrage.queryParam(name, ersetzePlatzhalter(wert)));
    response = anfrage.when().get("/kunden");
  }

  // ---------- Angenommen: existierende Kunden ----------

  @Angenommen("es existiert ein Kunde mit dem Namen {string}")
  public void esExistiertEinKunde(String name) {
    legeKundeAn(name, null, null);
  }

  @Angenommen("es existiert ein Kunde mit dem Namen {string} und dem Status {string}")
  public void esExistiertEinKundeMitStatus(String name, String status) {
    legeKundeAn(name, null, status);
  }

  @Angenommen("es existiert ein Kunde mit dem Namen {string} und dem Geburtsdatum {string}")
  public void esExistiertEinKundeMitGeburtsdatum(String name, String geburtsdatum) {
    legeKundeAn(name, geburtsdatum, null);
  }

  @Angenommen("es existiert ein Kunde mit dem Namen {string}, dem Geburtsdatum {string} und dem Status {string}")
  public void esExistiertEinKundeMitGeburtsdatumUndStatus(String name, String geburtsdatum, String status) {
    legeKundeAn(name, geburtsdatum, status);
  }

  /*
   * Legt einen Kunden an und löscht ihn wieder.
   * Die ID gehört damit garantiert zu keinem existierenden Kunden.
   */
  @Angenommen("es existiert ein gelöschter Kunde")
  public void esExistiertEinGeloeschterKunde() {
    legeKundeAn("Tom Mayer", null, null);
    given()
      .pathParam("id", id)
      .when()
      .delete("/kunden/{id}")
      .then()
      .statusCode(204);
  }

  // ---------- Angenommen: Kundendaten und Änderungen ----------

  @Angenommen("die Kundendaten enthalten den/das/die {word} {string}")
  public void dieKundendatenEnthalten(String attribut, String wert) {
    kundendaten.put(feldname(attribut), ersetzePlatzhalter(wert));
  }

  @Angenommen("die Kundendaten enthalten keinen Namen")
  public void dieKundendatenEnthaltenKeinenNamen() {
    kundendaten.remove("name");
  }

  @Angenommen("die Änderung setzt den/das/die {word} auf {string}")
  public void dieAenderungSetzt(String attribut, String wert) {
    aenderung.put(feldname(attribut), ersetzePlatzhalter(wert));
  }

  @Angenommen("die Änderung entfernt den/das/die {word}")
  public void dieAenderungEntfernt(String attribut) {
    aenderung.put(feldname(attribut), null);
  }

  // ---------- Wenn: Kunden suchen und abrufen ----------

  @Wenn("ich alle Kunden abrufe")
  public void ichAlleKundenAbrufe() {
    sucheKunden(Map.of());
  }

  @Wenn("ich alle Kunden im XML-Format abrufe")
  public void ichAlleKundenImXmlFormatAbrufe() {
    response = given()
      .accept(ContentType.XML)
      .when()
      .get("/kunden");
  }

  @Wenn("ich nach Kunden mit dem Namensteil {string} suche")
  public void ichNachKundenMitDemNamensteilSuche(String name) {
    sucheKunden(Map.of("name", name));
  }

  @Wenn("ich nach Kunden mit dem Status {string} suche")
  public void ichNachKundenMitDemStatusSuche(String status) {
    sucheKunden(Map.of("state", status));
  }

  @Wenn("ich nach Kunden mit dem Namensteil {string} und dem Status {string} suche")
  public void ichNachKundenMitDemNamensteilUndDemStatusSuche(String name, String status) {
    sucheKunden(Map.of("name", name, "state", status));
  }

  @Wenn("ich den Kunden abrufe")
  public void ichDenKundenAbrufe() {
    ichDenKundenMitDerIdAbrufe(id);
  }

  @Wenn("ich den Kunden mit der ID {string} abrufe")
  public void ichDenKundenMitDerIdAbrufe(String kundenId) {
    response = anfrage()
      .pathParam("id", kundenId)
      .when()
      .get("/kunden/{id}");
  }

  // ---------- Wenn: Kunden speichern ----------

  @Wenn("ich einen Kunden mit den Kundendaten speichere")
  public void ichEinenKundenMitDenKundendatenSpeichere() {
    response = anfrageMitJson(alsJson(kundendaten)).when().post("/kunden");
  }

  @Wenn("ich einen Kunden mit den Kundendaten speichere und die Antwort im XML-Format erwarte")
  public void ichEinenKundenSpeichereUndDieAntwortImXmlFormatErwarte() {
    response = given()
      .contentType(ContentType.JSON)
      .body(alsJson(kundendaten))
      .accept(ContentType.XML)
      .when()
      .post("/kunden");
  }

  @Wenn("ich einen Kunden mit den Kundendaten im XML-Format speichere")
  public void ichEinenKundenMitDenKundendatenImXmlFormatSpeichere() {
    response = anfrage()
      .contentType(ContentType.XML)
      .body(alsXml(kundendaten))
      .when()
      .post("/kunden");
  }

  @Wenn("ich einen Kunden ohne Daten speichere")
  public void ichEinenKundenOhneDatenSpeichere() {
    response = anfrage()
      .contentType(ContentType.JSON)
      .when()
      .post("/kunden");
  }

  @Wenn("ich einen Kunden mit fehlerhaft formatierten Daten speichere")
  public void ichEinenKundenMitFehlerhaftFormatiertenDatenSpeichere() {
    response = anfrageMitJson("{ \"name\": \"Tom Mayer\",").when().post("/kunden");
  }

  // ---------- Wenn: Kunden ersetzen, ändern und löschen ----------

  @Wenn("ich den Kunden durch die Kundendaten ersetze")
  public void ichDenKundenDurchDieKundendatenErsetze() {
    ichDenKundenMitDerIdDurchDieKundendatenErsetze(id);
  }

  @Wenn("ich den Kunden mit der ID {string} durch die Kundendaten ersetze")
  public void ichDenKundenMitDerIdDurchDieKundendatenErsetze(String kundenId) {
    response = anfrageMitJson(alsJson(kundendaten))
      .pathParam("id", kundenId)
      .when()
      .put("/kunden/{id}");
  }

  @Wenn("ich den Kunden ändere")
  public void ichDenKundenAendere() {
    ichDenKundenMitDerIdAendere(id);
  }

  @Wenn("ich den Kunden mit der ID {string} ändere")
  public void ichDenKundenMitDerIdAendere(String kundenId) {
    response = anfrage()
      .contentType(MERGE_PATCH_JSON)
      .body(alsJson(aenderung))
      .pathParam("id", kundenId)
      .when()
      .patch("/kunden/{id}");
  }

  @Wenn("ich den Kunden mit einer Änderung in einem nicht unterstützten Format ändere")
  public void ichDenKundenMitEinerAenderungInEinemNichtUnterstuetztenFormatAendere() {
    response = anfrageMitJson(alsJson(aenderung))
      .pathParam("id", id)
      .when()
      .patch("/kunden/{id}");
  }

  @Wenn("ich den Kunden lösche")
  public void ichDenKundenLoesche() {
    ichDenKundenMitDerIdLoesche(id);
  }

  @Wenn("ich den Kunden mit der ID {string} lösche")
  public void ichDenKundenMitDerIdLoesche(String kundenId) {
    response = anfrage()
      .pathParam("id", kundenId)
      .when()
      .delete("/kunden/{id}");
  }

  // ---------- Dann: Ergebnis des Vorgangs ----------

  @Dann("erhalte ich eine Liste von Kunden")
  public void erhalteIchEineListeVonKunden() {
    response.then()
      .statusCode(200)
      .body("$", instanceOf(Map.class))
      .body("items", is(notNullValue()))
      .body("items", is(instanceOf(List.class)));
  }

  @Dann("erhalte ich den Kunden")
  public void erhalteIchDenKunden() {
    response.then().statusCode(200);
  }

  @Dann("wird der Kunde angelegt")
  public void wirdDerKundeAngelegt() {
    response.then().statusCode(201);
  }

  @Dann("wird der Kunde ersetzt/geändert/gelöscht")
  public void wirdDerKundeVeraendert() {
    response.then().statusCode(204);
  }

  @Dann("wird der Kunde nicht gefunden")
  public void wirdDerKundeNichtGefunden() {
    response.then().statusCode(404);
  }

  @Dann("wird die Anfrage als ungültig abgelehnt")
  public void wirdDieAnfrageAlsUngueltigAbgelehnt() {
    response.then().statusCode(400);
  }

  @Dann("wird das Antwortformat nicht unterstützt")
  public void wirdDasAntwortformatNichtUnterstuetzt() {
    response.then().statusCode(406);
  }

  @Dann("wird das Datenformat nicht unterstützt")
  public void wirdDasDatenformatNichtUnterstuetzt() {
    response.then().statusCode(415);
  }

  // ---------- Dann: Inhalt der Antwort ----------

  @Dann("die Antwort ist im JSON-Format")
  public void dieAntwortIstImJsonFormat() {
    response.then().contentType(ContentType.JSON);
  }

  @Dann("der/das/die {word} des Kunden ist {string}")
  public void dasAttributDesKundenIst(String attribut, String wert) {
    response.then().body(feldname(attribut), is(equalTo(ersetzePlatzhalter(wert))));
  }

  @Dann("der/das/die {word} des Kunden ist nicht gesetzt")
  public void dasAttributDesKundenIstNichtGesetzt(String attribut) {
    response.then().body(feldname(attribut), is(nullValue()));
  }

  @Dann("der Kunde hat eine ID")
  public void derKundeHatEineId() {
    response.then().body("id", is(not(emptyOrNullString())));
  }

  @Dann("die Antwort verweist auf den neuen Kunden")
  public void dieAntwortVerweistAufDenNeuenKunden() {
    var angelegteId = response.jsonPath().getString("id");
    response.then().header("Location", is(equalTo("/kunden/" + angelegteId)));
  }

  @Dann("die Liste enthält den zuletzt angelegten Kunden")
  public void dieListeEnthaeltDenZuletztAngelegtenKunden() {
    response.then().body("items.id", hasItem(id));
  }

  @Dann("die Liste enthält genau {int} Kunden")
  public void dieListeEnthaeltGenauKunden(int anzahl) {
    response.then().body("items", hasSize(anzahl));
  }

  @Dann("alle Namen in der Liste enthalten {string}")
  public void alleNamenInDerListeEnthalten(String teil) {
    response.then().body("items.name", everyItem(containsStringIgnoringCase(ersetzePlatzhalter(teil))));
  }

  @Dann("alle Kunden in der Liste haben den Status {string}")
  public void alleKundenInDerListeHabenDenStatus(String status) {
    response.then().body("items.status", everyItem(is(equalTo(status))));
  }

}
