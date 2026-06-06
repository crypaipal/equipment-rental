package pl.pwr.miasi.equipmentrental.rental.infrastructure.persistence;

import jakarta.persistence.*;
import pl.pwr.miasi.equipmentrental.rental.domain.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class ReservationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "period_from", nullable = false)
    private Instant periodFrom;

    @Column(name = "period_to", nullable = false)
    private Instant periodTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReservationStatus status;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ReservationJpaEntity() {
    }

    public ReservationJpaEntity(
            UUID id,
            UUID userId,
            UUID assetId,
            Instant periodFrom,
            Instant periodTo,
            ReservationStatus status,
            String rejectionReason,
            Instant createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.assetId = assetId;
        this.periodFrom = periodFrom;
        this.periodTo = periodTo;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
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

    public Instant getPeriodFrom() {
        return periodFrom;
    }

    public Instant getPeriodTo() {
        return periodTo;
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