package de.schulung;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CustomersRepository implements PanacheRepositoryBase<Kunde, UUID> {
}
