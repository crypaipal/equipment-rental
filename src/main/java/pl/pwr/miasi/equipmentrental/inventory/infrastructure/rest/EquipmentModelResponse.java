package pl.pwr.miasi.equipmentrental.inventory.infrastructure.rest;

import java.util.UUID;

public record EquipmentModelResponse(
        UUID id,
        String name,
        String category,
        String manufacturer
) {
}