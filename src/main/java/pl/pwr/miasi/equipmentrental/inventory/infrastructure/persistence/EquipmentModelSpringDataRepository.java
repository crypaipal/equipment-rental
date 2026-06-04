package pl.pwr.miasi.equipmentrental.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EquipmentModelSpringDataRepository extends JpaRepository<EquipmentModelJpaEntity, UUID> {

    boolean existsByNameAndManufacturer(String name, String manufacturer);
}