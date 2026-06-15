package pl.pwr.miasi.equipmentrental.rental.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.rental.application.command.CancelReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalPeriod;
import pl.pwr.miasi.equipmentrental.rental.domain.Reservation;
import pl.pwr.miasi.equipmentrental.rental.domain.ReservationStatus;
import pl.pwr.miasi.equipmentrental.rental.domain.events.ReservationCancelledEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancelReservationServiceTest {

    private static final Instant PERIOD_FROM = Instant.parse("2026-06-15T10:00:00Z");
    private static final Instant PERIOD_TO = Instant.parse("2026-06-15T12:00:00Z");

    @Test
    void cancelsPendingReservationAndPublishesCancelledEvent() {
        Reservation reservation = pendingReservation();
        FakeReservationRepository reservationRepository = new FakeReservationRepository(reservation);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CancelReservationService service = new CancelReservationService(reservationRepository, eventPublisher);

        ReservationResult result = service.cancel(new CancelReservationCommand(reservation.getId()));

        assertThat(result.id()).isEqualTo(reservation.getId());
        assertThat(result.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(result.rejectionReason()).isNull();
        assertThat(reservationRepository.savedReservation).isSameAs(reservation);

        assertThat(eventPublisher.events).singleElement().isInstanceOf(ReservationCancelledEvent.class);
        ReservationCancelledEvent event = (ReservationCancelledEvent) eventPublisher.events.get(0);
        assertThat(event.reservationId()).isEqualTo(reservation.getId());
        assertThat(event.userId()).isEqualTo(reservation.getUserId());
        assertThat(event.assetId()).isEqualTo(reservation.getAssetId());
        assertThat(event.periodFrom()).isEqualTo(PERIOD_FROM);
        assertThat(event.periodTo()).isEqualTo(PERIOD_TO);
    }

    @Test
    void cancelsApprovedReservationAndPublishesCancelledEvent() {
        Reservation reservation = approvedReservation();
        FakeReservationRepository reservationRepository = new FakeReservationRepository(reservation);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CancelReservationService service = new CancelReservationService(reservationRepository, eventPublisher);

        ReservationResult result = service.cancel(new CancelReservationCommand(reservation.getId()));

        assertThat(result.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservationRepository.savedReservation).isSameAs(reservation);
        assertThat(eventPublisher.events).singleElement().isInstanceOf(ReservationCancelledEvent.class);
    }

    @Test
    void rejectsCancellationForFulfilledReservation() {
        Reservation reservation = approvedReservation();
        reservation.fulfill();
        FakeReservationRepository reservationRepository = new FakeReservationRepository(reservation);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CancelReservationService service = new CancelReservationService(reservationRepository, eventPublisher);

        assertThatThrownBy(() -> service.cancel(new CancelReservationCommand(reservation.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only pending or approved reservation can be cancelled");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.FULFILLED);
        assertThat(reservationRepository.savedReservation).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    @Test
    void rejectsCancellationWhenReservationDoesNotExist() {
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CancelReservationService service = new CancelReservationService(reservationRepository, eventPublisher);

        assertThatThrownBy(() -> service.cancel(new CancelReservationCommand(UUID.randomUUID())))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Reservation not found");

        assertThat(reservationRepository.savedReservation).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    private static Reservation approvedReservation() {
        Reservation reservation = pendingReservation();
        reservation.approve();
        return reservation;
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
