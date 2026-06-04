package pl.pwr.miasi.equipmentrental.inventory.application.result;

import java.util.UUID;

public record EquipmentModelResult(
        UUID id,
        String name,
        String category,
        String manufacturer
) {
}