package pl.pwr.miasi.equipmentrental.rental.domain.events;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ReservationCancelledEvent(
        UUID eventId,
        Instant occurredAt,
        UUID reservationId,
        UUID userId,
        UUID assetId,
        Instant periodFrom,
        Instant periodTo
) implements DomainEvent {

    public static ReservationCancelledEvent create(
            UUID reservationId,
            UUID userId,
            UUID assetId,
            Instant periodFrom,
            Instant periodTo
    ) {
        return new ReservationCancelledEvent(
                UUID.randomUUID(),
                Instant.now(),
                reservationId,
                userId,
                assetId,
                periodFrom,
                periodTo
        );
    }

    @Override
    public String eventType() {
        return "ReservationCancelledEvent";
    }
}