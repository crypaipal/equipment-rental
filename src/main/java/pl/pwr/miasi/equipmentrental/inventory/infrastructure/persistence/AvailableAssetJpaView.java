package pl.pwr.miasi.equipmentrental.inventory.infrastructure.persistence;

import java.util.UUID;

public record AvailableAssetJpaView(
        UUID assetId,
        UUID equipmentModelId,
        String inventoryTag,
        String modelName,
        String category,
        String manufacturer
) {
}