package de.schulung.customers;

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

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/kunden")
public class CustomersController {

  private static final String MERGE_PATCH_JSON = "application/merge-patch+json";
  private static final int NAME_MAX_LENGTH = 100;

  // TODO: Provisorium
  private final Map<UUID, Kunde> kunden = new ConcurrentHashMap<>();

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CustomerListResponse getKunden(
    @RequestParam(name = "name", required = false) String name,
    @RequestParam(name = "state", required = false) String state
  ) {
    if (name != null && (name.isEmpty() || name.length() > NAME_MAX_LENGTH)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    CustomerStatus status = null;
    if (state != null) {
      status = CustomerStatus
        .fromValue(state)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }
    var kundenObjekte = kunden
      .values()
      .stream()
      .filter(matchesName(name))
      .filter(matchesStatus(status))
      .toList();
    var response = new CustomerListResponse();
    response.setItems(kundenObjekte);
    return response;
  }

  private static Predicate<Kunde> matchesName(String name) {
    return kunde -> name == null
      || kunde.getName().toLowerCase().contains(name.toLowerCase());
  }

  private static Predicate<Kunde> matchesStatus(CustomerStatus status) {
    return kunde -> status == null || kunde.getStatus() == status;
  }

  // TODO ???
  private static final Pattern UUID_PATTERN = Pattern.compile(
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
  );

  private static UUID parseId(String id) {
    if (!UUID_PATTERN.matcher(id).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    return UUID.fromString(id);
  }

  private static boolean isValidName(String name) {
    return name != null && !name.isBlank() && name.length() <= NAME_MAX_LENGTH;
  }

  private static boolean isValid(Kunde kunde) {
    return kunde.getId() == null && isValidName(kunde.getName());
  }

  @GetMapping(
    value = "/{id}",
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Kunde findKundeById(@PathVariable("id") String id) {
    UUID uuid = parseId(id);
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
    if (!isValid(kunde)) {
      return ResponseEntity
        .badRequest()
        .build();
    }
    if (kunde.getStatus() == null) {
      kunde.setStatus(CustomerStatus.ACTIVE);
    }
    kunde.setId(UUID.randomUUID());
    kunden.put(kunde.getId(), kunde);
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
  public void replaceKunde(@PathVariable("id") String id, @RequestBody Kunde kunde) {
    UUID uuid = parseId(id);
    if (!isValid(kunde)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    if (!kunden.containsKey(uuid)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    if (kunde.getStatus() == null) {
      kunde.setStatus(CustomerStatus.ACTIVE);
    }
    kunde.setId(uuid);
    kunden.put(uuid, kunde);
  }

  @PatchMapping(
    value = "/{id}",
    consumes = MERGE_PATCH_JSON
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateKunde(@PathVariable("id") String id, @RequestBody Map<String, Object> patch) {
    UUID uuid = parseId(id);
    var kunde = kunden.get(uuid);
    if (kunde == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    // Änderungen auf einer Kopie anwenden, damit ein ungültiger Patch nichts verändert
    var geaendert = new Kunde();
    geaendert.setId(kunde.getId());
    geaendert.setName(kunde.getName());
    geaendert.setBirthdate(kunde.getBirthdate());
    geaendert.setStatus(kunde.getStatus());
    patch.forEach((feld, wert) -> applyPatch(geaendert, feld, wert));
    kunden.put(uuid, geaendert);
  }

  private static void applyPatch(Kunde kunde, String feld, Object wert) {
    switch (feld) {
      case "name" -> {
        if (!(wert instanceof String name) || !isValidName(name)) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        kunde.setName(name);
      }
      case "birthdate" -> {
        if (wert == null) {
          kunde.setBirthdate(null);
        } else if (wert instanceof String birthdate) {
          try {
            kunde.setBirthdate(LocalDate.parse(birthdate));
          } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
          }
        } else {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
      }
      case "status" -> {
        if (!(wert instanceof String status)) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        kunde.setStatus(
          CustomerStatus
            .fromValue(status)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST))
        );
      }
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteKunde(@PathVariable("id") String id) {
    UUID uuid = parseId(id);
    if (kunden.remove(uuid) == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

}
