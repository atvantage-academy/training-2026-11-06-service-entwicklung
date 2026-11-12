package de.schulung;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Path("/kunden")
public class CustomersResource {

  private static final String MERGE_PATCH_JSON = "application/merge-patch+json";
  private static final String UUID_PATTERN =
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

  // TODO: Provisorium
  private final Map<UUID, Kunde> kunden = new ConcurrentHashMap<>();

  @Inject
  ObjectMapper objectMapper;
  @Inject
  Validator validator;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CustomerListResponse getKunden(
    @QueryParam("name")
    @Size(min = 1, max = 100)
    String name,
    @QueryParam("state")
    @Pattern(regexp = "active|locked|disabled")
    String state,
    @Context UriInfo uriInfo
  ) {
    // Quarkus liefert für "?name=" null statt eines leeren Strings,
    // daher greift @Size hier nicht
    if (name == null && uriInfo.getQueryParameters().containsKey("name")) {
      throw new BadRequestException();
    }
    var kundenObjekte = kunden
      .values()
      .stream()
      .filter(matchesName(name))
      .filter(matchesStatus(state))
      .toList();
    var response = new CustomerListResponse();
    response.setItems(kundenObjekte);
    return response;
  }

  private static Predicate<Kunde> matchesName(String name) {
    return kunde -> name == null
      || kunde.getName().toLowerCase().contains(name.toLowerCase());
  }

  private static Predicate<Kunde> matchesStatus(String status) {
    return kunde -> status == null || status.equals(kunde.getStatus());
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Kunde findKundeById(
    @PathParam("id")
    @Pattern(regexp = UUID_PATTERN)
    String id
  ) {
    UUID uuid = UUID.fromString(id);
    var kunde = kunden.get(uuid);
    if(kunde == null) {
      throw new NotFoundException();
    }
    return kunde;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createKunde(@Valid @NotNull Kunde kunde) {
    if (kunde.getStatus() == null) {
      kunde.setStatus("active");
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
  public void replaceKunde(
    @PathParam("id")
    @Pattern(regexp = UUID_PATTERN)
    String id,
    @Valid @NotNull Kunde kunde
  ) {
    UUID uuid = UUID.fromString(id);
    if (!kunden.containsKey(uuid)) {
      throw new NotFoundException();
    }
    if (kunde.getStatus() == null) {
      kunde.setStatus("active");
    }
    kunde.setId(uuid);
    kunden.put(uuid, kunde);
  }

  @PATCH
  @Consumes(MERGE_PATCH_JSON)
  @Path("/{id}")
  public void updateKunde(
    @PathParam("id")
    @Pattern(regexp = UUID_PATTERN)
    String id,
    @NotNull JsonNode patch
  ) {
    UUID uuid = UUID.fromString(id);
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
    try {
      objectMapper
        .readerForUpdating(geaendert)
        .readValue(patch);
    } catch (IOException e) {
      throw new BadRequestException(e);
    }
    if (!validator.validate(geaendert).isEmpty()) {
      throw new BadRequestException();
    }
    kunden.put(uuid, geaendert);
  }

  @DELETE
  @Path("/{id}")
  public void deleteKunde(
    @PathParam("id")
    @Pattern(regexp = UUID_PATTERN)
    String id
  ) {
    UUID uuid = UUID.fromString(id);
    if (kunden.remove(uuid) == null) {
      throw new NotFoundException();
    }
  }

}
