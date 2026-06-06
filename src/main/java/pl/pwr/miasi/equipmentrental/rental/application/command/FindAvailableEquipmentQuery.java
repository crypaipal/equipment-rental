package pl.pwr.miasi.equipmentrental.rental.application.command;

import java.time.Instant;

public record FindAvailableEquipmentQuery(
        String category,
        Instant periodFrom,
        Instant periodTo
) {
}