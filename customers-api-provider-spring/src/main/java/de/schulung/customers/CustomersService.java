package de.schulung.customers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Validated
@Service
public class CustomersService {

  // TODO: Provisorium
  private final Map<UUID, Kunde> kunden = new ConcurrentHashMap<>();

  public Stream<Kunde> getCustomers() {
    return kunden
      .values()
      .stream();
  }

  public Optional<Kunde> getCustomerById(UUID id) {
    return Optional
      .ofNullable(kunden.get(id));
  }

  public void createCustomer(@Valid @NotNull Kunde kunde) {
    kunde.setId(UUID.randomUUID());
    kunden.put(kunde.getId(), kunde);
  }

}
