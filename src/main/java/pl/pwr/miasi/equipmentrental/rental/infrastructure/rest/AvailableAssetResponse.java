package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import java.util.UUID;

public record AvailableAssetResponse(
        UUID assetId,
        UUID equipmentModelId,
        String inventoryTag,
        String modelName,
        String category,
        String manufacturer
) {
}