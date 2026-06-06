package pl.pwr.miasi.equipmentrental.rental.domain.events;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record EquipmentReturnedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID rentalId,
        UUID reservationId,
        UUID userId,
        UUID assetId,
        Instant returnedAt,
        boolean damaged
) implements DomainEvent {

    public static EquipmentReturnedEvent create(
            UUID rentalId,
            UUID reservationId,
            UUID userId,
            UUID assetId,
            Instant returnedAt,
            boolean damaged
    ) {
        return new EquipmentReturnedEvent(
                UUID.randomUUID(),
                Instant.now(),
                rentalId,
                reservationId,
                userId,
                assetId,
                returnedAt,
                damaged
        );
    }

    @Override
    public String eventType() {
        return "EquipmentReturnedEvent";
    }
}