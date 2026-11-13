package de.schulung.customers.cucumber;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 *
 * Cucumber erzeugt für jedes Szenario eine neue Instanz dieser Klasse.
 */
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

  private final MockMvc mvc;
  private final ObjectMapper objectMapper;

  private final String eindeutig = UUID.randomUUID().toString();
  private String id;
  private final Map<String, Object> kundendaten = new LinkedHashMap<>();
  private final Map<String, Object> aenderung = new LinkedHashMap<>();
  private ResultActions response;

  public CustomersApiSteps(MockMvc mvc, ObjectMapper objectMapper) {
    this.mvc = mvc;
    this.objectMapper = objectMapper;
  }

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

  private static String alsXml(Map<String, Object> daten) {
    var xml = new StringBuilder("<kunde>");
    daten.forEach((feld, wert) -> xml.append("<%1$s>%2$s</%1$s>".formatted(feld, wert)));
    return xml.append("</kunde>").toString();
  }

  private void legeKundeAn(String name, String geburtsdatum, String status) throws Exception {
    var daten = new LinkedHashMap<String, Object>();
    daten.put("name", ersetzePlatzhalter(name));
    if (geburtsdatum != null) {
      daten.put("birthdate", ersetzePlatzhalter(geburtsdatum));
    }
    if (status != null) {
      daten.put("status", status);
    }
    var ergebnis = mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(daten))
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andReturn();
    id = JsonPath.read(ergebnis.getResponse().getContentAsString(), "$.id");
  }

  private void fuehreAus(MockHttpServletRequestBuilder anfrage) throws Exception {
    response = mvc.perform(anfrage.accept(MediaType.APPLICATION_JSON));
  }

  private void sucheKunden(Map<String, String> filter) throws Exception {
    var anfrage = get("/kunden");
    filter.forEach((name, wert) -> anfrage.queryParam(name, ersetzePlatzhalter(wert)));
    fuehreAus(anfrage);
  }

  // ---------- Angenommen: existierende Kunden ----------

  @Angenommen("es existiert ein Kunde mit dem Namen {string}")
  public void esExistiertEinKunde(String name) throws Exception {
    legeKundeAn(name, null, null);
  }

  @Angenommen("es existiert ein Kunde mit dem Namen {string} und dem Status {string}")
  public void esExistiertEinKundeMitStatus(String name, String status) throws Exception {
    legeKundeAn(name, null, status);
  }

  @Angenommen("es existiert ein Kunde mit dem Namen {string} und dem Geburtsdatum {string}")
  public void esExistiertEinKundeMitGeburtsdatum(String name, String geburtsdatum) throws Exception {
    legeKundeAn(name, geburtsdatum, null);
  }

  @Angenommen("es existiert ein Kunde mit dem Namen {string}, dem Geburtsdatum {string} und dem Status {string}")
  public void esExistiertEinKundeMitGeburtsdatumUndStatus(String name, String geburtsdatum, String status) throws Exception {
    legeKundeAn(name, geburtsdatum, status);
  }

  /*
   * Legt einen Kunden an und löscht ihn wieder.
   * Die ID gehört damit garantiert zu keinem existierenden Kunden.
   */
  @Angenommen("es existiert ein gelöschter Kunde")
  public void esExistiertEinGeloeschterKunde() throws Exception {
    legeKundeAn("Tom Mayer", null, null);
    mvc.perform(delete("/kunden/{id}", id))
      .andExpect(status().isNoContent());
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
  public void ichAlleKundenAbrufe() throws Exception {
    sucheKunden(Map.of());
  }

  @Wenn("ich alle Kunden im XML-Format abrufe")
  public void ichAlleKundenImXmlFormatAbrufe() throws Exception {
    response = mvc.perform(get("/kunden").accept(MediaType.APPLICATION_XML));
  }

  @Wenn("ich nach Kunden mit dem Namensteil {string} suche")
  public void ichNachKundenMitDemNamensteilSuche(String name) throws Exception {
    sucheKunden(Map.of("name", name));
  }

  @Wenn("ich nach Kunden mit dem Status {string} suche")
  public void ichNachKundenMitDemStatusSuche(String status) throws Exception {
    sucheKunden(Map.of("state", status));
  }

  @Wenn("ich nach Kunden mit dem Namensteil {string} und dem Status {string} suche")
  public void ichNachKundenMitDemNamensteilUndDemStatusSuche(String name, String status) throws Exception {
    sucheKunden(Map.of("name", name, "state", status));
  }

  @Wenn("ich den Kunden abrufe")
  public void ichDenKundenAbrufe() throws Exception {
    ichDenKundenMitDerIdAbrufe(id);
  }

  @Wenn("ich den Kunden mit der ID {string} abrufe")
  public void ichDenKundenMitDerIdAbrufe(String kundenId) throws Exception {
    fuehreAus(get("/kunden/{id}", kundenId));
  }

  // ---------- Wenn: Kunden speichern ----------

  @Wenn("ich einen Kunden mit den Kundendaten speichere")
  public void ichEinenKundenMitDenKundendatenSpeichere() throws Exception {
    fuehreAus(
      post("/kunden")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(kundendaten))
    );
  }

  @Wenn("ich einen Kunden mit den Kundendaten speichere und die Antwort im XML-Format erwarte")
  public void ichEinenKundenSpeichereUndDieAntwortImXmlFormatErwarte() throws Exception {
    response = mvc.perform(
      post("/kunden")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(kundendaten))
        .accept(MediaType.APPLICATION_XML)
    );
  }

  @Wenn("ich einen Kunden mit den Kundendaten im XML-Format speichere")
  public void ichEinenKundenMitDenKundendatenImXmlFormatSpeichere() throws Exception {
    fuehreAus(
      post("/kunden")
        .contentType(MediaType.APPLICATION_XML)
        .content(alsXml(kundendaten))
    );
  }

  @Wenn("ich einen Kunden ohne Daten speichere")
  public void ichEinenKundenOhneDatenSpeichere() throws Exception {
    fuehreAus(
      post("/kunden")
        .contentType(MediaType.APPLICATION_JSON)
    );
  }

  @Wenn("ich einen Kunden mit fehlerhaft formatierten Daten speichere")
  public void ichEinenKundenMitFehlerhaftFormatiertenDatenSpeichere() throws Exception {
    fuehreAus(
      post("/kunden")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"name\": \"Tom Mayer\",")
    );
  }

  // ---------- Wenn: Kunden ersetzen, ändern und löschen ----------

  @Wenn("ich den Kunden durch die Kundendaten ersetze")
  public void ichDenKundenDurchDieKundendatenErsetze() throws Exception {
    ichDenKundenMitDerIdDurchDieKundendatenErsetze(id);
  }

  @Wenn("ich den Kunden mit der ID {string} durch die Kundendaten ersetze")
  public void ichDenKundenMitDerIdDurchDieKundendatenErsetze(String kundenId) throws Exception {
    fuehreAus(
      put("/kunden/{id}", kundenId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(kundendaten))
    );
  }

  @Wenn("ich den Kunden ändere")
  public void ichDenKundenAendere() throws Exception {
    ichDenKundenMitDerIdAendere(id);
  }

  @Wenn("ich den Kunden mit der ID {string} ändere")
  public void ichDenKundenMitDerIdAendere(String kundenId) throws Exception {
    fuehreAus(
      patch("/kunden/{id}", kundenId)
        .contentType(MERGE_PATCH_JSON)
        .content(objectMapper.writeValueAsString(aenderung))
    );
  }

  @Wenn("ich den Kunden mit einer Änderung in einem nicht unterstützten Format ändere")
  public void ichDenKundenMitEinerAenderungInEinemNichtUnterstuetztenFormatAendere() throws Exception {
    fuehreAus(
      patch("/kunden/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(aenderung))
    );
  }

  @Wenn("ich den Kunden lösche")
  public void ichDenKundenLoesche() throws Exception {
    ichDenKundenMitDerIdLoesche(id);
  }

  @Wenn("ich den Kunden mit der ID {string} lösche")
  public void ichDenKundenMitDerIdLoesche(String kundenId) throws Exception {
    fuehreAus(delete("/kunden/{id}", kundenId));
  }

  // ---------- Dann: Ergebnis des Vorgangs ----------

  @Dann("erhalte ich eine Liste von Kunden")
  public void erhalteIchEineListeVonKunden() throws Exception {
    response
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isMap())
      .andExpect(jsonPath("$.items").isArray());
  }

  @Dann("erhalte ich den Kunden")
  public void erhalteIchDenKunden() throws Exception {
    response.andExpect(status().isOk());
  }

  @Dann("wird der Kunde angelegt")
  public void wirdDerKundeAngelegt() throws Exception {
    response.andExpect(status().isCreated());
  }

  @Dann("wird der Kunde ersetzt/geändert/gelöscht")
  public void wirdDerKundeVeraendert() throws Exception {
    response.andExpect(status().isNoContent());
  }

  @Dann("wird der Kunde nicht gefunden")
  public void wirdDerKundeNichtGefunden() throws Exception {
    response.andExpect(status().isNotFound());
  }

  @Dann("wird die Anfrage als ungültig abgelehnt")
  public void wirdDieAnfrageAlsUngueltigAbgelehnt() throws Exception {
    response.andExpect(status().isBadRequest());
  }

  @Dann("wird das Antwortformat nicht unterstützt")
  public void wirdDasAntwortformatNichtUnterstuetzt() throws Exception {
    response.andExpect(status().isNotAcceptable());
  }

  @Dann("wird das Datenformat nicht unterstützt")
  public void wirdDasDatenformatNichtUnterstuetzt() throws Exception {
    response.andExpect(status().isUnsupportedMediaType());
  }

  // ---------- Dann: Inhalt der Antwort ----------

  @Dann("die Antwort ist im JSON-Format")
  public void dieAntwortIstImJsonFormat() throws Exception {
    response.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Dann("der/das/die {word} des Kunden ist {string}")
  public void dasAttributDesKundenIst(String attribut, String wert) throws Exception {
    response.andExpect(jsonPath("$." + feldname(attribut), is(equalTo(ersetzePlatzhalter(wert)))));
  }

  @Dann("der/das/die {word} des Kunden ist nicht gesetzt")
  public void dasAttributDesKundenIstNichtGesetzt(String attribut) throws Exception {
    response.andExpect(jsonPath("$." + feldname(attribut), is(nullValue())));
  }

  @Dann("der Kunde hat eine ID")
  public void derKundeHatEineId() throws Exception {
    response.andExpect(jsonPath("$.id", is(not(emptyOrNullString()))));
  }

  @Dann("die Antwort verweist auf den neuen Kunden")
  public void dieAntwortVerweistAufDenNeuenKunden() throws Exception {
    String angelegteId = JsonPath.read(response.andReturn().getResponse().getContentAsString(), "$.id");
    response.andExpect(header().string("Location", "/kunden/" + angelegteId));
  }

  @Dann("die Liste enthält den zuletzt angelegten Kunden")
  public void dieListeEnthaeltDenZuletztAngelegtenKunden() throws Exception {
    response.andExpect(jsonPath("$.items[*].id", hasItem(id)));
  }

  @Dann("die Liste enthält genau {int} Kunden")
  public void dieListeEnthaeltGenauKunden(int anzahl) throws Exception {
    response.andExpect(jsonPath("$.items", hasSize(anzahl)));
  }

  @Dann("alle Namen in der Liste enthalten {string}")
  public void alleNamenInDerListeEnthalten(String teil) throws Exception {
    response.andExpect(jsonPath("$.items[*].name", everyItem(containsStringIgnoringCase(ersetzePlatzhalter(teil)))));
  }

  @Dann("alle Kunden in der Liste haben den Status {string}")
  public void alleKundenInDerListeHabenDenStatus(String status) throws Exception {
    response.andExpect(jsonPath("$.items[*].status", everyItem(is(equalTo(status)))));
  }

}
