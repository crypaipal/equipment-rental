package pl.pwr.miasi.equipmentrental.rental.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.rental.application.command.ReviewReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalPeriod;
import pl.pwr.miasi.equipmentrental.rental.domain.Reservation;
import pl.pwr.miasi.equipmentrental.rental.domain.ReservationStatus;
import pl.pwr.miasi.equipmentrental.rental.domain.events.ReservationApprovedEvent;
import pl.pwr.miasi.equipmentrental.rental.domain.events.ReservationRejectedEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewReservationServiceTest {

    private static final Instant PERIOD_FROM = Instant.parse("2026-06-15T10:00:00Z");
    private static final Instant PERIOD_TO = Instant.parse("2026-06-15T12:00:00Z");

    @Test
    void approvesPendingReservationAndPublishesApprovedEvent() {
        Reservation reservation = pendingReservation();
        FakeReservationRepository reservationRepository = new FakeReservationRepository(reservation);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ReviewReservationService service = new ReviewReservationService(reservationRepository, eventPublisher);

        ReservationResult result = service.review(new ReviewReservationCommand(
                reservation.getId(),
                true,
                null
        ));

        assertThat(result.id()).isEqualTo(reservation.getId());
        assertThat(result.status()).isEqualTo(ReservationStatus.APPROVED);
        assertThat(result.rejectionReason()).isNull();
        assertThat(reservationRepository.savedReservation).isSameAs(reservation);

        assertThat(eventPublisher.events).singleElement().isInstanceOf(ReservationApprovedEvent.class);
        ReservationApprovedEvent event = (ReservationApprovedEvent) eventPublisher.events.get(0);
        assertThat(event.reservationId()).isEqualTo(reservation.getId());
        assertThat(event.userId()).isEqualTo(reservation.getUserId());
        assertThat(event.assetId()).isEqualTo(reservation.getAssetId());
    }

    @Test
    void rejectsPendingReservationAndPublishesRejectedEvent() {
        Reservation reservation = pendingReservation();
        FakeReservationRepository reservationRepository = new FakeReservationRepository(reservation);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ReviewReservationService service = new ReviewReservationService(reservationRepository, eventPublisher);

        ReservationResult result = service.review(new ReviewReservationCommand(
                reservation.getId(),
                false,
                "Asset needed for classes"
        ));

        assertThat(result.id()).isEqualTo(reservation.getId());
        assertThat(result.status()).isEqualTo(ReservationStatus.REJECTED);
        assertThat(result.rejectionReason()).isEqualTo("Asset needed for classes");
        assertThat(reservationRepository.savedReservation).isSameAs(reservation);

        assertThat(eventPublisher.events).singleElement().isInstanceOf(ReservationRejectedEvent.class);
        ReservationRejectedEvent event = (ReservationRejectedEvent) eventPublisher.events.get(0);
        assertThat(event.reservationId()).isEqualTo(reservation.getId());
        assertThat(event.userId()).isEqualTo(reservation.getUserId());
        assertThat(event.assetId()).isEqualTo(reservation.getAssetId());
        assertThat(event.rejectionReason()).isEqualTo("Asset needed for classes");
    }

    @Test
    void rejectsReviewWhenReservationDoesNotExist() {
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ReviewReservationService service = new ReviewReservationService(reservationRepository, eventPublisher);

        assertThatThrownBy(() -> service.review(new ReviewReservationCommand(
                UUID.randomUUID(),
                true,
                null
        )))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Reservation not found");

        assertThat(reservationRepository.savedReservation).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    private static Reservation pendingReservation() {
        return Reservation.request(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new RentalPeriod(PERIOD_FROM, PERIOD_TO)
        );
    }

    private static class FakeReservationRepository implements ReservationRepository {
        private final Reservation reservation;
        private Reservation savedReservation;

        private FakeReservationRepository() {
            this(null);
        }

        private FakeReservationRepository(Reservation reservation) {
            this.reservation = reservation;
        }

        @Override
        public Reservation save(Reservation reservation) {
            savedReservation = reservation;
            return reservation;
        }

        @Override
        public Optional<Reservation> findById(UUID id) {
            if (reservation != null && reservation.getId().equals(id)) {
                return Optional.of(reservation);
            }

            return Optional.empty();
        }

        @Override
        public boolean existsActiveReservationForAsset(UUID assetId, RentalPeriod rentalPeriod) {
            return false;
        }

        @Override
        public List<UUID> findReservedAssetIds(RentalPeriod rentalPeriod) {
            return List.of();
        }

        @Override
        public List<Reservation> findAll() {
            return reservation == null ? List.of() : List.of(reservation);
        }
    }

    private static class RecordingEventPublisher implements EventPublisher {
        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }
    }
}
