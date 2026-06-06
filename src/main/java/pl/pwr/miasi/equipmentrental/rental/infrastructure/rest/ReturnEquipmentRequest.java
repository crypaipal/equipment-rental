package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import java.time.Instant;

public record ReturnEquipmentRequest(
        Boolean damaged,
        String damageReport,
        Instant returnedAt
) {
}