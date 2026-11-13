package de.schulung.customers;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

// TODO: we could create a record here
@Getter
@Setter
public class CustomerListResponse {

  private Collection<Kunde> items;

}
