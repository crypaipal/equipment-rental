package pl.pwr.miasi.equipmentrental.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;

import java.util.List;
import java.util.UUID;

public interface AssetSpringDataRepository extends JpaRepository<AssetJpaEntity, UUID> {

    boolean existsByInventoryTag(String inventoryTag);

    @Query("""
            select new pl.pwr.miasi.equipmentrental.inventory.infrastructure.persistence.AvailableAssetJpaView(
                a.id,
                a.equipmentModelId,
                a.inventoryTag,
                m.name,
                m.category,
                m.manufacturer
            )
            from AssetJpaEntity a, EquipmentModelJpaEntity m
            where a.equipmentModelId = m.id
              and a.condition = :condition
              and lower(m.category) = lower(:category)
            """)
    List<AvailableAssetJpaView> findOperationalAssetsByCategory(
            String category,
            AssetCondition condition
    );
}