package pl.pwr.miasi.equipmentrental.inventory.application.dto;

import java.util.UUID;

public record AvailableAssetDto(
        UUID assetId,
        UUID equipmentModelId,
        String inventoryTag,
        String modelName,
        String category,
        String manufacturer
) {}