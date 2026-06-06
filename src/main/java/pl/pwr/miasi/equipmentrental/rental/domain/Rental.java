package pl.pwr.miasi.equipmentrental.rental.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;
import java.util.UUID;

public class Rental {

    private final UUID id;
    private final UUID reservationId;
    private final UUID userId;
    private final UUID assetId;
    private final Instant checkoutAt;
    private final Instant expectedReturnAt;
    private Instant returnedAt;
    private RentalStatus status;

    public Rental(
            UUID id,
            UUID reservationId,
            UUID userId,
            UUID assetId,
            Instant checkoutAt,
            Instant expectedReturnAt,
            Instant returnedAt,
            RentalStatus status
    ) {
        if (id == null) {
            throw new BusinessException("Rental id cannot be null");
        }

        if (reservationId == null) {
            throw new BusinessException("Reservation id cannot be null");
        }

        if (userId == null) {
            throw new BusinessException("Rental user id cannot be null");
        }

        if (assetId == null) {
            throw new BusinessException("Rental asset id cannot be null");
        }

        if (checkoutAt == null) {
            throw new BusinessException("Checkout date cannot be null");
        }

        if (expectedReturnAt == null || !expectedReturnAt.isAfter(checkoutAt)) {
            throw new BusinessException("Expected return date must be after checkout date");
        }

        if (status == null) {
            throw new BusinessException("Rental status cannot be null");
        }

        this.id = id;
        this.reservationId = reservationId;
        this.userId = userId;
        this.assetId = assetId;
        this.checkoutAt = checkoutAt;
        this.expectedReturnAt = expectedReturnAt;
        this.returnedAt = returnedAt;
        this.status = status;
    }

    public static Rental checkout(Reservation reservation) {
        Instant checkoutAt = Instant.now();

        return new Rental(
                UUID.randomUUID(),
                reservation.getId(),
                reservation.getUserId(),
                reservation.getAssetId(),
                checkoutAt,
                reservation.getRentalPeriod().to(),
                null,
                RentalStatus.ACTIVE
        );
    }

    public void returnEquipment(Instant returnedAt) {
        if (status != RentalStatus.ACTIVE) {
            throw new BusinessException("Only active rental can be returned");
        }

        if (returnedAt == null) {
            throw new BusinessException("Return date cannot be null");
        }

        if (returnedAt.isBefore(checkoutAt)) {
            throw new BusinessException("Return date cannot be before checkout date");
        }

        this.returnedAt = returnedAt;
        this.status = RentalStatus.CLOSED;
    }

    public boolean isOverdue() {
        return returnedAt != null && returnedAt.isAfter(expectedReturnAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public Instant getCheckoutAt() {
        return checkoutAt;
    }

    public Instant getExpectedReturnAt() {
        return expectedReturnAt;
    }

    public Instant getReturnedAt() {
        return returnedAt;
    }

    public RentalStatus getStatus() {
        return status;
    }
}