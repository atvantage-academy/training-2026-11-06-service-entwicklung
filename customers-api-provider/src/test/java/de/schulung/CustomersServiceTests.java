package de.schulung;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@TestTransaction
class CustomersServiceTests {

  @Inject
  CustomersService customersService;

  @Test
  void given_invalid_customer_when_createCustomer_then_validation_exception() {
    Kunde kunde = new Kunde();
    kunde.setName(null); // invalid: no name

    assertThrows(
      Exception.class,
      () -> customersService.createCustomer(kunde)
    );
  }

  @Test
  void when_createCustomer_with_null_then_validation_exception() {
    assertThrows(
      Exception.class,
      () -> customersService.createCustomer(null)
    );
  }

}
