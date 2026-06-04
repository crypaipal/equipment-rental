package pl.pwr.miasi.equipmentrental.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssetSpringDataRepository extends JpaRepository<AssetJpaEntity, UUID> {

    boolean existsByInventoryTag(String inventoryTag);
}