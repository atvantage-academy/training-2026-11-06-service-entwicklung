package de.schulung.customers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/kunden")
public class CustomersController {

  // TODO: Provisorium
  private final Map<UUID, Kunde> kunden = new ConcurrentHashMap<>();

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CustomerListResponse getKunden() {
    var kundenObjekte = kunden.values();
    var response = new CustomerListResponse();
    response.setItems(kundenObjekte);
    return response;
  }

  @GetMapping(
    value = "/{id}",
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Kunde findKundeById(
    @PathVariable("id")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    String id
  ) {
    UUID uuid = UUID.fromString(id);
    var kunde = kunden.get(uuid);
    if (kunde == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return kunde;
  }

  @PostMapping(
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Kunde> createKunde(@Valid @NotNull @RequestBody Kunde kunde) {
    kunde.setId(UUID.randomUUID());
    kunden.put(kunde.getId(), kunde);
    URI location = URI.create("/kunden/" + kunde.getId());
    return ResponseEntity
      .created(location)
      .body(kunde);
  }

}
