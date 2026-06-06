package pl.pwr.miasi.equipmentrental.rental.domain.events;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record RentalOverdueEvent(
        UUID eventId,
        Instant occurredAt,
        UUID rentalId,
        UUID userId,
        UUID assetId,
        Instant expectedReturnAt,
        Instant actualReturnAt
) implements DomainEvent {

    public static RentalOverdueEvent create(
            UUID rentalId,
            UUID userId,
            UUID assetId,
            Instant expectedReturnAt,
            Instant actualReturnAt
    ) {
        return new RentalOverdueEvent(
                UUID.randomUUID(),
                Instant.now(),
                rentalId,
                userId,
                assetId,
                expectedReturnAt,
                actualReturnAt
        );
    }

    @Override
    public String eventType() {
        return "RentalOverdueEvent";
    }
}