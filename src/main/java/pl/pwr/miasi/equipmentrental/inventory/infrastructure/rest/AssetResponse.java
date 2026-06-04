package pl.pwr.miasi.equipmentrental.inventory.infrastructure.rest;

import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;

import java.util.UUID;

public record AssetResponse(
        UUID id,
        UUID equipmentModelId,
        String inventoryTag,
        AssetCondition condition,
        String damageReport
) {
}