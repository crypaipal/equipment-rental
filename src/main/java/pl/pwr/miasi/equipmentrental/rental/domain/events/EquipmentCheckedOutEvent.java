package pl.pwr.miasi.equipmentrental.rental.domain.events;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record EquipmentCheckedOutEvent(
        UUID eventId,
        Instant occurredAt,
        UUID rentalId,
        UUID reservationId,
        UUID userId,
        UUID assetId,
        Instant expectedReturnAt
) implements DomainEvent {

    public static EquipmentCheckedOutEvent create(
            UUID rentalId,
            UUID reservationId,
            UUID userId,
            UUID assetId,
            Instant expectedReturnAt
    ) {
        return new EquipmentCheckedOutEvent(
                UUID.randomUUID(),
                Instant.now(),
                rentalId,
                reservationId,
                userId,
                assetId,
                expectedReturnAt
        );
    }

    @Override
    public String eventType() {
        return "EquipmentCheckedOutEvent";
    }
}