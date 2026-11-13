package de.schulung;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@ApplicationScoped
public class CustomersService {

  private final CustomersRepository repo;

  public CustomersService(CustomersRepository repo) {
    this.repo = repo;
  }

  public Stream<Kunde> getCustomers() {
    // return repo.streamAll();
    return repo.listAll().stream();
  }

  public Optional<Kunde> getCustomerById(UUID id) {
    return repo.findByIdOptional(id);
  }

  @Transactional
  public void createCustomer(@Valid Kunde kunde) {
    repo.persist(kunde);
  }



}
