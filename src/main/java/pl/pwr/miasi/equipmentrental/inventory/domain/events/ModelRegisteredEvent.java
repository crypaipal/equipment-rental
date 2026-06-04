package pl.pwr.miasi.equipmentrental.inventory.domain.events;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ModelRegisteredEvent(
        UUID eventId,
        Instant occurredAt,
        UUID equipmentModelId,
        String name,
        String category,
        String manufacturer
) implements DomainEvent {

    public static ModelRegisteredEvent create(
            UUID equipmentModelId,
            String name,
            String category,
            String manufacturer
    ) {
        return new ModelRegisteredEvent(
                UUID.randomUUID(),
                Instant.now(),
                equipmentModelId,
                name,
                category,
                manufacturer
        );
    }

    @Override
    public String eventType() {
        return "ModelRegisteredEvent";
    }
}