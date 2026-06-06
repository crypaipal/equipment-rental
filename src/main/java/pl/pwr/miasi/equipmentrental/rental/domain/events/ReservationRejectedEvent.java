package pl.pwr.miasi.equipmentrental.rental.domain.events;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ReservationRejectedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID reservationId,
        UUID userId,
        UUID assetId,
        String rejectionReason
) implements DomainEvent {

    public static ReservationRejectedEvent create(
            UUID reservationId,
            UUID userId,
            UUID assetId,
            String rejectionReason
    ) {
        return new ReservationRejectedEvent(
                UUID.randomUUID(),
                Instant.now(),
                reservationId,
                userId,
                assetId,
                rejectionReason
        );
    }

    @Override
    public String eventType() {
        return "ReservationRejectedEvent";
    }
}