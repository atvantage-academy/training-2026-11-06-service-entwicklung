package de.schulung;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Path("/kunden")
public class CustomersResource {

  private static final String MERGE_PATCH_JSON = "application/merge-patch+json";
  private static final int NAME_MAX_LENGTH = 100;

  // TODO: Provisorium
  private final Map<UUID, Kunde> kunden = new ConcurrentHashMap<>();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CustomerListResponse getKunden(
    @QueryParam("name") String name,
    @QueryParam("state") String state,
    @Context UriInfo uriInfo
  ) {
    // Quarkus liefert für "?name=" null statt eines leeren Strings
    if (name == null && uriInfo.getQueryParameters().containsKey("name")) {
      throw new BadRequestException();
    }
    if (name != null && (name.isEmpty() || name.length() > NAME_MAX_LENGTH)) {
      throw new BadRequestException();
    }
    CustomerStatus status = null;
    if (state != null) {
      status = CustomerStatus
        .fromValue(state)
        .orElseThrow(BadRequestException::new);
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
    if(!UUID_PATTERN.matcher(id).matches()) {
      throw new BadRequestException();
    }
    return UUID.fromString(id);
  }

  private static boolean isValidName(String name) {
    return name != null && !name.isBlank() && name.length() <= NAME_MAX_LENGTH;
  }

  private static boolean isValid(Kunde kunde) {
    return kunde != null && kunde.getId() == null && isValidName(kunde.getName());
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Kunde findKundeById(@PathParam("id") String id) {
    UUID uuid = parseId(id);
    var kunde = kunden.get(uuid);
    if(kunde == null) {
      throw new NotFoundException();
    }
    return kunde;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createKunde(Kunde kunde) {
    if(!isValid(kunde)) {
      return Response
        .status(Response.Status.BAD_REQUEST)
        .build();
    }
    if (kunde.getStatus() == null) {
      kunde.setStatus(CustomerStatus.ACTIVE);
    }
    kunde.setId(UUID.randomUUID());
    kunden.put(kunde.getId(), kunde);
    URI location = URI.create("/kunden/" + kunde.getId());
    return Response
      .status(Response.Status.CREATED)
      .header("Location", location)
      .entity(kunde)
      .build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public void replaceKunde(@PathParam("id") String id, Kunde kunde) {
    UUID uuid = parseId(id);
    if (!isValid(kunde)) {
      throw new BadRequestException();
    }
    if (!kunden.containsKey(uuid)) {
      throw new NotFoundException();
    }
    if (kunde.getStatus() == null) {
      kunde.setStatus(CustomerStatus.ACTIVE);
    }
    kunde.setId(uuid);
    kunden.put(uuid, kunde);
  }

  @PATCH
  @Consumes(MERGE_PATCH_JSON)
  @Path("/{id}")
  public void updateKunde(@PathParam("id") String id, Map<String, Object> patch) {
    UUID uuid = parseId(id);
    if (patch == null) {
      throw new BadRequestException();
    }
    var kunde = kunden.get(uuid);
    if (kunde == null) {
      throw new NotFoundException();
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
          throw new BadRequestException();
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
            throw new BadRequestException();
          }
        } else {
          throw new BadRequestException();
        }
      }
      case "status" -> {
        if (!(wert instanceof String status)) {
          throw new BadRequestException();
        }
        kunde.setStatus(
          CustomerStatus
            .fromValue(status)
            .orElseThrow(BadRequestException::new)
        );
      }
      default -> throw new BadRequestException();
    }
  }

  @DELETE
  @Path("/{id}")
  public void deleteKunde(@PathParam("id") String id) {
    UUID uuid = parseId(id);
    if (kunden.remove(uuid) == null) {
      throw new NotFoundException();
    }
  }

}
