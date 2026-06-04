package pl.pwr.miasi.equipmentrental.inventory.application.command;

import java.util.UUID;

public record RegisterAssetCommand(
        UUID equipmentModelId,
        String inventoryTag
) {
}