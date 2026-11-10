package de.schulung;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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

}
