package de.schulung.customers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Validated
@Service
@RequiredArgsConstructor
public class CustomersService {

  private static final String DEFAULT_STATUS = "active";

  private final CustomersRepository repo;

  public Stream<Kunde> getCustomers(String name, String status) {
    return repo.search(name, status).stream();
  }

  public Optional<Kunde> getCustomerById(UUID id) {
    return repo.findById(id);
  }

  @Transactional
  public void createCustomer(@Valid @NotNull Kunde kunde) {
    if (kunde.getStatus() == null) {
      kunde.setStatus(DEFAULT_STATUS);
    }
    repo.save(kunde);
  }

  /**
   * Ersetzt die Daten eines vorhandenen Kunden.
   *
   * @return {@code false}, wenn es keinen Kunden mit dieser ID gibt
   */
  @Transactional
  public boolean replaceCustomer(UUID id, @Valid @NotNull Kunde kunde) {
    var vorhanden = repo.findById(id);
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
    if (!repo.existsById(id)) {
      return false;
    }
    repo.deleteById(id);
    return true;
  }

}
