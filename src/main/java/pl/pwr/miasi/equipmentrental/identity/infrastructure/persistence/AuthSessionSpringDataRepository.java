package pl.pwr.miasi.equipmentrental.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthSessionSpringDataRepository extends JpaRepository<AuthSessionJpaEntity, UUID> {
}