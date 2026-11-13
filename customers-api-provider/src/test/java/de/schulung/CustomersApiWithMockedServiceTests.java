package de.schulung;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
public class CustomersApiWithMockedServiceTests {

  @InjectMock
  CustomersService customersService;

  @Test
  void given_customer_exists_when_get_customer_by_id_then_return_200_and_customer() {

    // SETUP

    UUID id = UUID.randomUUID();
    Kunde kunde = new Kunde();
    kunde.setId(id);
    kunde.setName("Tom Mayer");
    kunde.setBirthdate(LocalDate.of(1990, Month.JANUARY, 1));
    kunde.setStatus("active");

    when(customersService.getCustomerById(id))
        .thenReturn(Optional.of(kunde));

    // TEST

    given()
      .accept(ContentType.JSON)
      .pathParam("id", id)
      .when()
      .get("/kunden/{id}")
      .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("id", is(equalTo(id.toString())))
      .body("name", is(equalTo("Tom Mayer")))
      .body("birthdate", is(equalTo("1990-01-01")))
      .body("status", is(equalTo("active")));

  }

  @Test
  void given_customer_does_not_exist_when_get_customer_by_id_then_return_404() {

    UUID id = UUID.randomUUID();

    when(customersService.getCustomerById(id))
        .thenReturn(Optional.empty());

    given()
      .accept(ContentType.JSON)
      .pathParam("id", id)
      .when()
      .get("/kunden/{id}")
      .then()
      .statusCode(404);

  }

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

    verifyNoInteractions(customersService);

  }

}
