package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record RequestReservationRequest(
        @NotNull UUID userId,
        @NotNull UUID assetId,
        @NotNull Instant periodFrom,
        @NotNull Instant periodTo
) {
}