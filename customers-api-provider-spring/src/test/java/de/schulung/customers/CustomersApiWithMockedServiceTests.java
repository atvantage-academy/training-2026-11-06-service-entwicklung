package de.schulung.customers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomersApiWithMockedServiceTests {

  @Autowired
  MockMvc mvc;

  @MockitoBean
  CustomersService customersService;

  @Test
  void given_customer_exists_when_get_customer_by_id_then_return_200_and_customer() throws Exception {

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

    mvc.perform(
        get("/kunden/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value(id.toString()))
      .andExpect(jsonPath("$.name").value("Tom Mayer"))
      .andExpect(jsonPath("$.birthdate").value("1990-01-01"))
      .andExpect(jsonPath("$.status").value("active"));

  }

  @Test
  void given_customer_does_not_exist_when_get_customer_by_id_then_return_404() throws Exception {

    UUID id = UUID.randomUUID();

    when(customersService.getCustomerById(id))
      .thenReturn(Optional.empty());

    mvc.perform(
        get("/kunden/{id}", id)
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound());

  }

  @Test
  void given_empty_customer_when_post_kunden_then_return_400() throws Exception {
    mvc.perform(
        post("/kunden")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{}")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());

    verifyNoInteractions(customersService);

  }

}
