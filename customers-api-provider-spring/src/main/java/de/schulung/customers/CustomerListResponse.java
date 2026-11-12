package de.schulung.customers;

import java.util.Collection;

public record CustomerListResponse(
  Collection<Kunde> items
) {

}
