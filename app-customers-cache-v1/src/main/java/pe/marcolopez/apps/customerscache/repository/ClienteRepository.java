package pe.marcolopez.apps.customerscache.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import pe.marcolopez.apps.customerscache.entity.Cliente;
import java.util.UUID;
import java.util.Optional;

@ApplicationScoped
public class ClienteRepository implements PanacheRepositoryBase<Cliente, UUID> {

    public Optional<Cliente> findByEmail(String email) {
        return find("LOWER(email) = LOWER(?1)", email).firstResultOptional();
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
}