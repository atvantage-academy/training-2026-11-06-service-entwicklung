package de.schulung.customers;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class CustomersServiceTests {

  @Autowired
  CustomersService customersService;

  @Test
  void given_invalid_customer_when_createCustomer_then_validation_exception() {
    Kunde kunde = new Kunde();
    kunde.setName(null); // invalid: no name

    assertThrows(
      ConstraintViolationException.class,
      () -> customersService.createCustomer(kunde)
    );
  }

  @Test
  void when_createCustomer_with_null_then_validation_exception() {
    assertThrows(
      ConstraintViolationException.class,
      () -> customersService.createCustomer(null)
    );
  }

}
