package pl.pwr.miasi.equipmentrental.inventory.infrastructure.rest;

import jakarta.validation.constraints.NotNull;
import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;

public record ChangeAssetConditionRequest(
        @NotNull AssetCondition condition,
        String damageReport
) {
}