package de.schulung;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@ApplicationScoped
public class CustomersService {

  private static final String DEFAULT_STATUS = "active";

  private final CustomersRepository repo;

  public CustomersService(CustomersRepository repo) {
    this.repo = repo;
  }

  public Stream<Kunde> getCustomers(String name, String status) {
    // nicht gesetzte Filter (null) schränken die Suche nicht ein
    return repo
      .list(
        "(:name is null or lower(name) like concat('%', lower(:name), '%'))"
          + " and (:status is null or status = :status)",
        Parameters
          .with("name", name)
          .and("status", status)
      )
      .stream();
  }

  public Optional<Kunde> getCustomerById(UUID id) {
    return repo.findByIdOptional(id);
  }

  @Transactional
  public void createCustomer(@Valid Kunde kunde) {
    if (kunde.getStatus() == null) {
      kunde.setStatus(DEFAULT_STATUS);
    }
    repo.persist(kunde);
  }

  /**
   * Ersetzt die Daten eines vorhandenen Kunden.
   *
   * @return {@code false}, wenn es keinen Kunden mit dieser ID gibt
   */
  @Transactional
  public boolean replaceCustomer(UUID id, @Valid Kunde kunde) {
    var vorhanden = repo.findByIdOptional(id);
    if (vorhanden.isEmpty()) {
      return false;
    }
    // die Entity ist managed, die Änderungen werden beim Commit geschrieben
    var entity = vorhanden.get();
    entity.setName(kunde.getName());
    entity.setBirthdate(kunde.getBirthdate());
    entity.setStatus(kunde.getStatus() == null ? DEFAULT_STATUS : kunde.getStatus());
    return true;
  }

  /**
   * Löscht einen Kunden.
   *
   * @return {@code false}, wenn es keinen Kunden mit dieser ID gibt
   */
  @Transactional
  public boolean deleteCustomer(UUID id) {
    return repo.deleteById(id);
  }

}
