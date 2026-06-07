package pl.pwr.miasi.equipmentrental.rental.domain.events;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record EquipmentReturnedWithDamageEvent(
        UUID eventId,
        Instant occurredAt,
        UUID rentalId,
        UUID userId,
        UUID assetId,
        String damageReport
) implements DomainEvent {

    public static EquipmentReturnedWithDamageEvent create(
            UUID rentalId,
            UUID userId,
            UUID assetId,
            String damageReport
    ) {
        return new EquipmentReturnedWithDamageEvent(
                UUID.randomUUID(),
                Instant.now(),
                rentalId,
                userId,
                assetId,
                damageReport
        );
    }

    @Override
    public String eventType() {
        return "EquipmentReturnedWithDamageEvent";
    }
}