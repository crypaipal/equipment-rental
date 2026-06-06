package pl.pwr.miasi.equipmentrental.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserSpringDataRepository extends JpaRepository<UserJpaEntity, UUID> {

    boolean existsByEmail(String email);
}