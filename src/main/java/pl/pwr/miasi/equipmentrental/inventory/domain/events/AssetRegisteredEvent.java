package pl.pwr.miasi.equipmentrental.inventory.domain.events;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AssetRegisteredEvent(
        UUID eventId,
        Instant occurredAt,
        UUID assetId,
        UUID equipmentModelId,
        String inventoryTag
) implements DomainEvent {

    public static AssetRegisteredEvent create(
            UUID assetId,
            UUID equipmentModelId,
            String inventoryTag
    ) {
        return new AssetRegisteredEvent(
                UUID.randomUUID(),
                Instant.now(),
                assetId,
                equipmentModelId,
                inventoryTag
        );
    }

    @Override
    public String eventType() {
        return "AssetRegisteredEvent";
    }
}