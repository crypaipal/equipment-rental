package pl.pwr.miasi.equipmentrental.inventory.application.result;

import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;

import java.util.UUID;

public record AssetResult(
        UUID id,
        UUID equipmentModelId,
        String inventoryTag,
        AssetCondition condition,
        String damageReport
) {
}