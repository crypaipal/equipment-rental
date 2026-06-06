package pl.pwr.miasi.equipmentrental.rental.application.command;

import java.time.Instant;
import java.util.UUID;

public record RequestReservationCommand(
        UUID userId,
        UUID assetId,
        Instant periodFrom,
        Instant periodTo
) {
}