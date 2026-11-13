package de.schulung.customers;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/kunden")
public class CustomersController {

  private static final String MERGE_PATCH_JSON = "application/merge-patch+json";
  private static final String UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

  private final CustomersService customersService;
  private final ObjectMapper objectMapper;
  private final Validator validator;

  public CustomersController(
    CustomersService customersService,
    ObjectMapper objectMapper,
    Validator validator
  ) {
    this.customersService = customersService;
    this.objectMapper = objectMapper;
    this.validator = validator;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CustomerListResponse getKunden(
    @RequestParam(name = "name", required = false)
    @Size(min = 1, max = 100)
    String name,
    @RequestParam(name = "state", required = false)
    @Pattern(regexp = "active|locked|disabled")
    String state
  ) {
    var kundenObjekte = customersService
      .getCustomers(name, state)
      .toList();
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
    @Pattern(regexp = UUID_PATTERN)
    String id
  ) {
    UUID uuid = UUID.fromString(id);
    return customersService
      .getCustomerById(uuid)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @PostMapping(
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Kunde> createKunde(@Valid @NotNull @RequestBody Kunde kunde) {
    customersService.createCustomer(kunde);
    URI location = URI.create("/kunden/" + kunde.getId());
    return ResponseEntity
      .created(location)
      .body(kunde);
  }

  @PutMapping(
    value = "/{id}",
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void replaceKunde(
    @PathVariable("id")
    @Pattern(regexp = UUID_PATTERN)
    String id,
    @Valid @NotNull @RequestBody Kunde kunde
  ) {
    UUID uuid = UUID.fromString(id);
    if (!customersService.replaceCustomer(uuid, kunde)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @PatchMapping(
    value = "/{id}",
    consumes = MERGE_PATCH_JSON
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateKunde(
    @PathVariable("id")
    @Pattern(regexp = UUID_PATTERN)
    String id,
    @RequestBody JsonNode patch
  ) {
    UUID uuid = UUID.fromString(id);
    var kunde = customersService
      .getCustomerById(uuid)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    // Änderungen auf einer Kopie anwenden, damit ein ungültiger Patch nichts verändert
    var geaendert = new Kunde();
    geaendert.setId(kunde.getId());
    geaendert.setName(kunde.getName());
    geaendert.setBirthdate(kunde.getBirthdate());
    geaendert.setStatus(kunde.getStatus());
    try {
      objectMapper
        .readerForUpdating(geaendert)
        .readValue(patch);
    } catch (JacksonException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, null, e);
    }
    if (!validator.validate(geaendert).isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    if (!customersService.replaceCustomer(uuid, geaendert)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteKunde(
    @PathVariable("id")
    @Pattern(regexp = UUID_PATTERN)
    String id
  ) {
    UUID uuid = UUID.fromString(id);
    if (!customersService.deleteCustomer(uuid)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

}
