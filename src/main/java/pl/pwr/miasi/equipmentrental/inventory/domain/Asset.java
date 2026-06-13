package pl.pwr.miasi.equipmentrental.inventory.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.util.UUID;

public class Asset {

    private final UUID id;
    private final UUID equipmentModelId;
    private final InventoryTag inventoryTag;
    private AssetCondition condition;
    private DamageReport damageReport;

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
        this.damageReport = damageReport == null || damageReport.isBlank()
                ? null
                : DamageReport.create(damageReport);
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

    public void reportDamage(DamageReport report, AssetConditionRule rule) {
        if (report == null) {
            throw new BusinessException("Damage report cannot be null");
        }

        if (rule == null) {
            throw new BusinessException("Asset condition rule cannot be null");
        }

        this.condition = AssetCondition.DAMAGED;
        this.damageReport = report;

        if (rule.canBeAssignedToRent(this.condition)) {
            throw new BusinessException("Damaged asset cannot be assigned to rent");
        }
    }

    public void markAsDamaged(String damageReport) {
        reportDamage(DamageReport.create(damageReport), new AssetConditionRule());
    }

    public void markAsRepaired() {
        this.condition = AssetCondition.OPERATIONAL;
        this.damageReport = null;
    }

    public void repair() {
        markAsRepaired();
    }

    public void sendToRepair(String damageReport) {
        if (damageReport == null || damageReport.isBlank()) {
            throw new BusinessException("Damage report cannot be empty when sending asset to repair");
        }

        this.condition = AssetCondition.IN_REPAIR;
        this.damageReport = DamageReport.create(damageReport);
    }

    public boolean isAvailableForRental() {
        return new AssetConditionRule().canBeAssignedToRent(condition);
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
        return damageReport == null ? null : damageReport.getDescription();
    }

    public DamageReport getDamageReportDetails() {
        return damageReport;
    }
}