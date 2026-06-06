package pl.pwr.miasi.equipmentrental.inventory.application.command;

import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;

import java.util.UUID;

public record ChangeAssetConditionCommand(
        UUID assetId,
        AssetCondition condition,
        String damageReport
) {
}