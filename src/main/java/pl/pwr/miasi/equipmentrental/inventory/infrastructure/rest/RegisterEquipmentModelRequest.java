package pl.pwr.miasi.equipmentrental.inventory.infrastructure.rest;

import jakarta.validation.constraints.NotBlank;

public record RegisterEquipmentModelRequest(
        @NotBlank String name,
        @NotBlank String category,
        @NotBlank String manufacturer
) {
}