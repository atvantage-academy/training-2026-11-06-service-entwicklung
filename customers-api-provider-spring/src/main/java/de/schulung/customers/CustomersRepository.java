package de.schulung.customers;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomersRepository extends JpaRepository<Kunde, UUID> {
}
