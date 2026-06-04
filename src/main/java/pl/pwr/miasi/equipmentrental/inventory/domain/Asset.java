package pl.pwr.miasi.equipmentrental.inventory.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.util.UUID;

public class Asset {

    private final UUID id;
    private final UUID equipmentModelId;
    private final InventoryTag inventoryTag;
    private AssetCondition condition;
    private String damageReport;

    public Asset(UUID id, UUID equipmentModelId, InventoryTag inventoryTag, AssetCondition condition, String damageReport) {
        if (id == null) {
            throw new BusinessException("Asset id cannot be null");
        }

        if (equipmentModelId == null) {
            throw new BusinessException("Equipment model id cannot be null");
        }

        if (inventoryTag == null) {
            throw new BusinessException("Inventory tag cannot be null");
        }

        this.id = id;
        this.equipmentModelId = equipmentModelId;
        this.inventoryTag = inventoryTag;
        this.condition = condition == null ? AssetCondition.OPERATIONAL : condition;
        this.damageReport = damageReport;
    }

    public static Asset register(UUID equipmentModelId, InventoryTag inventoryTag) {
        return new Asset(
                UUID.randomUUID(),
                equipmentModelId,
                inventoryTag,
                AssetCondition.OPERATIONAL,
                null
        );
    }

    public void markAsDamaged(String damageReport) {
        if (damageReport == null || damageReport.isBlank()) {
            throw new BusinessException("Damage report cannot be empty when marking asset as damaged");
        }

        this.condition = AssetCondition.DAMAGED;
        this.damageReport = damageReport;
    }

    public void markAsRepaired() {
        this.condition = AssetCondition.OPERATIONAL;
        this.damageReport = null;
    }

    public void sendToRepair(String damageReport) {
        if (damageReport == null || damageReport.isBlank()) {
            throw new BusinessException("Damage report cannot be empty when sending asset to repair");
        }

        this.condition = AssetCondition.IN_REPAIR;
        this.damageReport = damageReport;
    }

    public boolean isAvailableForRental() {
        return condition == AssetCondition.OPERATIONAL;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEquipmentModelId() {
        return equipmentModelId;
    }

    public InventoryTag getInventoryTag() {
        return inventoryTag;
    }

    public AssetCondition getCondition() {
        return condition;
    }

    public String getDamageReport() {
        return damageReport;
    }
}