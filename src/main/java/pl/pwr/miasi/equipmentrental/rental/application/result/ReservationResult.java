package pl.pwr.miasi.equipmentrental.rental.application.result;

import pl.pwr.miasi.equipmentrental.rental.domain.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

public record ReservationResult(
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