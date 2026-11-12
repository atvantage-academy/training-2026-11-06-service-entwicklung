package de.schulung;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Path("/kunden")
public class CustomersResource {

  // TODO: Provisorium
  private final Map<UUID, Kunde> kunden = new ConcurrentHashMap<>();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
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

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Kunde findByKundeById(@PathParam("id") String id) {
    if(!UUID_PATTERN.matcher(id).matches()) {
      throw new BadRequestException();
    }
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
  public Response createKunde(Kunde kunde) {
    if(kunde == null) {
      return Response
        .status(Response.Status.BAD_REQUEST)
        .build();
    }
    if (kunde.getId() != null) {
      return Response
        .status(Response.Status.BAD_REQUEST)
        .build();
    }
    if(kunde.getName() == null || kunde.getName().isBlank()) {
      return Response
        .status(Response.Status.BAD_REQUEST)
        .build();
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

}
