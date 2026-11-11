package de.schulung;

import java.util.Collection;

public record CustomerListResponse(
  Collection<Kunde> items
) {

}
