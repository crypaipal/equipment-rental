package pl.pwr.miasi.equipmentrental.rental.application.result;

import java.util.UUID;

public record AvailableAssetResult(
        UUID assetId,
        UUID equipmentModelId,
        String inventoryTag,
        String modelName,
        String category,
        String manufacturer
) {
}