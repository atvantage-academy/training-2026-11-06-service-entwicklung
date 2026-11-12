package de.schulung;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Kunde findKundeById(
    @PathParam("id")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
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
