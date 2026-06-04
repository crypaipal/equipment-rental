package pl.pwr.miasi.equipmentrental.inventory.infrastructure.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegisterAssetRequest(
        @NotNull UUID equipmentModelId,
        @NotBlank String inventoryTag
) {
}