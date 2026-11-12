package de.schulung.customers;

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
import java.util.regex.Pattern;

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

  // TODO ???
  private static final Pattern UUID_PATTERN = Pattern.compile(
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
  );

  @GetMapping(
    value = "/{id}",
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Kunde findKundeById(@PathVariable("id") String id) {
    if (!UUID_PATTERN.matcher(id).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
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
  public ResponseEntity<Kunde> createKunde(@RequestBody Kunde kunde) {
    if (kunde.getId() != null) {
      return ResponseEntity
        .badRequest()
        .build();
    }
    if (kunde.getName() == null || kunde.getName().isBlank()) {
      return ResponseEntity
        .badRequest()
        .build();
    }
    kunde.setId(UUID.randomUUID());
    kunden.put(kunde.getId(), kunde);
    URI location = URI.create("/kunden/" + kunde.getId());
    return ResponseEntity
      .created(location)
      .body(kunde);
  }

}
