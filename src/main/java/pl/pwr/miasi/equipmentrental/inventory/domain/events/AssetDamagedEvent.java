package pl.pwr.miasi.equipmentrental.inventory.domain.events;

import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AssetDamagedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID assetId,
        String inventoryTag,
        AssetCondition condition,
        String damageReport
) implements DomainEvent {

    public static AssetDamagedEvent create(
            UUID assetId,
            String inventoryTag,
            AssetCondition condition,
            String damageReport
    ) {
        return new AssetDamagedEvent(
                UUID.randomUUID(),
                Instant.now(),
                assetId,
                inventoryTag,
                condition,
                damageReport
        );
    }

    @Override
    public String eventType() {
        return "AssetDamagedEvent";
    }
}