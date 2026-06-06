package pl.pwr.miasi.equipmentrental.rental.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RentalSpringDataRepository extends JpaRepository<RentalJpaEntity, UUID> {
}