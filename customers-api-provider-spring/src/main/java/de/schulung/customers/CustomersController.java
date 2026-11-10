package de.schulung.customers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kunden")
public class CustomersController {

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public String getKunden() {
    return """
      {
        "items": ["Hello, World!"]
      }
      """;
  }

}
