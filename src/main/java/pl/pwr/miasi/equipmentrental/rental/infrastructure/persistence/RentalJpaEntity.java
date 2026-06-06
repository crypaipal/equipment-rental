package pl.pwr.miasi.equipmentrental.rental.infrastructure.persistence;

import jakarta.persistence.*;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rentals")
public class RentalJpaEntity {

    @Id
    private UUID id;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "checkout_at", nullable = false)
    private Instant checkoutAt;

    @Column(name = "expected_return_at", nullable = false)
    private Instant expectedReturnAt;

    @Column(name = "returned_at")
    private Instant returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RentalStatus status;

    protected RentalJpaEntity() {
    }

    public RentalJpaEntity(
            UUID id,
            UUID reservationId,
            UUID userId,
            UUID assetId,
            Instant checkoutAt,
            Instant expectedReturnAt,
            Instant returnedAt,
            RentalStatus status
    ) {
        this.id = id;
        this.reservationId = reservationId;
        this.userId = userId;
        this.assetId = assetId;
        this.checkoutAt = checkoutAt;
        this.expectedReturnAt = expectedReturnAt;
        this.returnedAt = returnedAt;
        this.status = status;
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