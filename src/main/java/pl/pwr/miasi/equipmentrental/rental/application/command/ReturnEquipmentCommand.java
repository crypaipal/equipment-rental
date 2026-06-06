package pl.pwr.miasi.equipmentrental.rental.application.command;

import java.time.Instant;
import java.util.UUID;

public record ReturnEquipmentCommand(
        UUID rentalId,
        boolean damaged,
        String damageReport,
        Instant returnedAt
) {
}