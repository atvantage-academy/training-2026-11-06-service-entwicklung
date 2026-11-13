package de.schulung.customers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Validated
@Service
public class CustomersService {

  private final CustomersRepository repo;

  public CustomersService(CustomersRepository repo) {
    this.repo = repo;
  }

  public Stream<Kunde> getCustomers() {
    return repo.findAll().stream();
  }

  public Optional<Kunde> getCustomerById(UUID id) {
    return repo.findById(id);
  }

  @Transactional
  public void createCustomer(@Valid @NotNull Kunde kunde) {
    repo.save(kunde);
  }

}
