package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import pl.pwr.miasi.equipmentrental.rental.domain.RentalStatus;

import java.time.Instant;
import java.util.UUID;

public record RentalResponse(
        UUID id,
        UUID reservationId,
        UUID userId,
        UUID assetId,
        Instant checkoutAt,
        Instant expectedReturnAt,
        Instant returnedAt,
        RentalStatus status
) {
}