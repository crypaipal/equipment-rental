package pl.pwr.miasi.equipmentrental.inventory.infrastructure.persistence;

import jakarta.persistence.*;
import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;

import java.util.UUID;

@Entity
@Table(name = "assets")
public class AssetJpaEntity {

    @Id
    private UUID id;

    @Column(name = "equipment_model_id", nullable = false)
    private UUID equipmentModelId;

    @Column(name = "inventory_tag", nullable = false, unique = true, length = 100)
    private String inventoryTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssetCondition condition;

    @Column(name = "damage_report", length = 500)
    private String damageReport;

    protected AssetJpaEntity() {
    }

    public AssetJpaEntity(
            UUID id,
            UUID equipmentModelId,
            String inventoryTag,
            AssetCondition condition,
            String damageReport
    ) {
        this.id = id;
        this.equipmentModelId = equipmentModelId;
        this.inventoryTag = inventoryTag;
        this.condition = condition;
        this.damageReport = damageReport;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEquipmentModelId() {
        return equipmentModelId;
    }

    public String getInventoryTag() {
        return inventoryTag;
    }

    public AssetCondition getCondition() {
        return condition;
    }

    public String getDamageReport() {
        return damageReport;
    }
}