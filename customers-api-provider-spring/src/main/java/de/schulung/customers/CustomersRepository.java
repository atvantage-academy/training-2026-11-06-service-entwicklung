package de.schulung.customers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CustomersRepository extends JpaRepository<Kunde, UUID> {

  // nicht gesetzte Filter (null) schränken die Suche nicht ein
  @Query("""
    select c from Customer c
    where (:name is null or lower(c.name) like concat('%', lower(:name), '%'))
      and (:status is null or c.status = :status)
    """)
  List<Kunde> search(String name, String status);

}
