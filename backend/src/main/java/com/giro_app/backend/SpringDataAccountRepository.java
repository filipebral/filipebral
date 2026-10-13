package com.giro_app.backend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SpringDataAccountRepository extends JpaRepository<Account, UUID>, AccountRepository {
    // O JpaRepository já fornece o save() e findById() exigidos pela interface AccountRepository
}