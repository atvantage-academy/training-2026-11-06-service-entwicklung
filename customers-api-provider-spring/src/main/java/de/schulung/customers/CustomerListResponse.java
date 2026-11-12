package de.schulung.customers;

import java.util.Collection;

// TODO: we could create a record here
public class CustomerListResponse {

  private Collection<Kunde> items;


  public Collection<Kunde> getItems() {
    return items;
  }

  public void setItems(Collection<Kunde> items) {
    this.items = items;
  }
}
