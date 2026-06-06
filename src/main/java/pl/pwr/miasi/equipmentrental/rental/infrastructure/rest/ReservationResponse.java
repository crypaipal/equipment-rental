package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import pl.pwr.miasi.equipmentrental.rental.domain.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID userId,
        UUID assetId,
        Instant periodFrom,
        Instant periodTo,
        ReservationStatus status,
        String rejectionReason,
        Instant createdAt
) {
}