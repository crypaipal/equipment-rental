package pl.pwr.miasi.equipmentrental.rental.application.port.out;

import java.util.UUID;

public record AvailableAssetView(
        UUID assetId,
        UUID equipmentModelId,
        String inventoryTag,
        String modelName,
        String category,
        String manufacturer
) {
}