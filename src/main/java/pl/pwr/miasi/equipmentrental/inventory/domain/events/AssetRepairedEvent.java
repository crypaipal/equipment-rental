package pl.pwr.miasi.equipmentrental.inventory.domain.events;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AssetRepairedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID assetId,
        String inventoryTag
) implements DomainEvent {

    public static AssetRepairedEvent create(UUID assetId, String inventoryTag) {
        return new AssetRepairedEvent(
                UUID.randomUUID(),
                Instant.now(),
                assetId,
                inventoryTag
        );
    }

    @Override
    public String eventType() {
        return "AssetRepairedEvent";
    }
}