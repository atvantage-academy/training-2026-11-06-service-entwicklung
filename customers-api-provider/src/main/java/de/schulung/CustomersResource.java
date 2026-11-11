package de.schulung;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/kunden")
public class CustomersResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getKunden() {
    return """
      { 
        "items": ["Hello, World!"]
      }
      """;
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
    if(kunde.getName() == null || kunde.getName().isBlank()) {
      return Response
        .status(Response.Status.BAD_REQUEST)
        .build();
    }
    kunde.setId(UUID.randomUUID());
    return Response
      .status(Response.Status.CREATED)
      .entity(kunde)
      .build();
  }

}
