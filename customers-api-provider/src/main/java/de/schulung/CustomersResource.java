package de.schulung;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
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
  public Kunde findByKundeById(@PathParam("id") String id) {
    UUID uuid = UUID.fromString(id);
    return kunden.get(uuid);
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
