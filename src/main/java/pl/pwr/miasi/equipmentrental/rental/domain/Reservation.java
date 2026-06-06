package pl.pwr.miasi.equipmentrental.rental.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;
import java.util.UUID;

public class Reservation {

    private final UUID id;
    private final UUID userId;
    private final UUID assetId;
    private final RentalPeriod rentalPeriod;
    private ReservationStatus status;
    private String rejectionReason;
    private final Instant createdAt;

    public Reservation(
            UUID id,
            UUID userId,
            UUID assetId,
            RentalPeriod rentalPeriod,
            ReservationStatus status,
            String rejectionReason,
            Instant createdAt
    ) {
        if (id == null) {
            throw new BusinessException("Reservation id cannot be null");
        }

        if (userId == null) {
            throw new BusinessException("Reservation user id cannot be null");
        }

        if (assetId == null) {
            throw new BusinessException("Reservation asset id cannot be null");
        }

        if (rentalPeriod == null) {
            throw new BusinessException("Rental period cannot be null");
        }

        if (status == null) {
            throw new BusinessException("Reservation status cannot be null");
        }

        if (createdAt == null) {
            throw new BusinessException("Reservation creation date cannot be null");
        }

        this.id = id;
        this.userId = userId;
        this.assetId = assetId;
        this.rentalPeriod = rentalPeriod;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
    }

    public static Reservation request(UUID userId, UUID assetId, RentalPeriod rentalPeriod) {
        return new Reservation(
                UUID.randomUUID(),
                userId,
                assetId,
                rentalPeriod,
                ReservationStatus.PENDING,
                null,
                Instant.now()
        );
    }

    public void approve() {
        if (status != ReservationStatus.PENDING) {
            throw new BusinessException("Only pending reservation can be approved");
        }

        this.status = ReservationStatus.APPROVED;
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        if (status != ReservationStatus.PENDING) {
            throw new BusinessException("Only pending reservation can be rejected");
        }

        this.status = ReservationStatus.REJECTED;
        this.rejectionReason = reason == null || reason.isBlank() ? null : reason;
    }

    public void fulfill() {
        if (status != ReservationStatus.APPROVED) {
            throw new BusinessException("Only approved reservation can be fulfilled");
        }

        this.status = ReservationStatus.FULFILLED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public RentalPeriod getRentalPeriod() {
        return rentalPeriod;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}