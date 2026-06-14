package pl.pwr.miasi.equipmentrental.rental.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.rental.application.command.CheckoutEquipmentCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.AvailableAssetView;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.RentalRepository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;
import pl.pwr.miasi.equipmentrental.rental.domain.Rental;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalPeriod;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalStatus;
import pl.pwr.miasi.equipmentrental.rental.domain.Reservation;
import pl.pwr.miasi.equipmentrental.rental.domain.ReservationStatus;
import pl.pwr.miasi.equipmentrental.rental.domain.events.EquipmentCheckedOutEvent;
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

class CheckoutEquipmentServiceTest {

    private static final Instant PERIOD_FROM = Instant.parse("2026-06-15T10:00:00Z");
    private static final Instant PERIOD_TO = Instant.parse("2026-06-15T12:00:00Z");

    @Test
    void checksOutApprovedReservationAndPublishesEvent() {
        Reservation reservation = approvedReservation();
        FakeReservationRepository reservationRepository = new FakeReservationRepository(reservation);
        FakeRentalRepository rentalRepository = new FakeRentalRepository();
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(true);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CheckoutEquipmentService service = new CheckoutEquipmentService(
                reservationRepository,
                rentalRepository,
                inventoryAssetAccessPort,
                eventPublisher
        );

        RentalResult result = service.checkout(new CheckoutEquipmentCommand(reservation.getId()));

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.FULFILLED);
        assertThat(reservationRepository.savedReservation).isSameAs(reservation);
        assertThat(rentalRepository.savedRental).isNotNull();

        assertThat(result.id()).isEqualTo(rentalRepository.savedRental.getId());
        assertThat(result.reservationId()).isEqualTo(reservation.getId());
        assertThat(result.userId()).isEqualTo(reservation.getUserId());
        assertThat(result.assetId()).isEqualTo(reservation.getAssetId());
        assertThat(result.expectedReturnAt()).isEqualTo(PERIOD_TO);
        assertThat(result.returnedAt()).isNull();
        assertThat(result.status()).isEqualTo(RentalStatus.ACTIVE);

        assertThat(eventPublisher.events).singleElement().isInstanceOf(EquipmentCheckedOutEvent.class);
        EquipmentCheckedOutEvent event = (EquipmentCheckedOutEvent) eventPublisher.events.get(0);
        assertThat(event.rentalId()).isEqualTo(result.id());
        assertThat(event.reservationId()).isEqualTo(reservation.getId());
        assertThat(event.userId()).isEqualTo(reservation.getUserId());
        assertThat(event.assetId()).isEqualTo(reservation.getAssetId());
        assertThat(event.expectedReturnAt()).isEqualTo(PERIOD_TO);
    }

    @Test
    void rejectsCheckoutWhenReservationDoesNotExist() {
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        FakeRentalRepository rentalRepository = new FakeRentalRepository();
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(true);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CheckoutEquipmentService service = new CheckoutEquipmentService(
                reservationRepository,
                rentalRepository,
                inventoryAssetAccessPort,
                eventPublisher
        );

        assertThatThrownBy(() -> service.checkout(new CheckoutEquipmentCommand(UUID.randomUUID())))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Reservation not found");

        assertThat(reservationRepository.savedReservation).isNull();
        assertThat(rentalRepository.savedRental).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    @Test
    void rejectsCheckoutWhenAssetIsNotAvailable() {
        Reservation reservation = approvedReservation();
        FakeReservationRepository reservationRepository = new FakeReservationRepository(reservation);
        FakeRentalRepository rentalRepository = new FakeRentalRepository();
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(false);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CheckoutEquipmentService service = new CheckoutEquipmentService(
                reservationRepository,
                rentalRepository,
                inventoryAssetAccessPort,
                eventPublisher
        );

        assertThatThrownBy(() -> service.checkout(new CheckoutEquipmentCommand(reservation.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Asset is not available for checkout");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.APPROVED);
        assertThat(reservationRepository.savedReservation).isNull();
        assertThat(rentalRepository.savedRental).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    @Test
    void rejectsCheckoutForPendingReservation() {
        Reservation reservation = pendingReservation();
        FakeReservationRepository reservationRepository = new FakeReservationRepository(reservation);
        FakeRentalRepository rentalRepository = new FakeRentalRepository();
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(true);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CheckoutEquipmentService service = new CheckoutEquipmentService(
                reservationRepository,
                rentalRepository,
                inventoryAssetAccessPort,
                eventPublisher
        );

        assertThatThrownBy(() -> service.checkout(new CheckoutEquipmentCommand(reservation.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only approved reservation can be fulfilled");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservationRepository.savedReservation).isNull();
        assertThat(rentalRepository.savedRental).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    @Test
    void rejectsCheckoutForCancelledReservation() {
        Reservation reservation = pendingReservation();
        reservation.cancel();
        FakeReservationRepository reservationRepository = new FakeReservationRepository(reservation);
        FakeRentalRepository rentalRepository = new FakeRentalRepository();
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(true);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        CheckoutEquipmentService service = new CheckoutEquipmentService(
                reservationRepository,
                rentalRepository,
                inventoryAssetAccessPort,
                eventPublisher
        );

        assertThatThrownBy(() -> service.checkout(new CheckoutEquipmentCommand(reservation.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only approved reservation can be fulfilled");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservationRepository.savedReservation).isNull();
        assertThat(rentalRepository.savedRental).isNull();
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

    private static class FakeRentalRepository implements RentalRepository {
        private Rental savedRental;

        @Override
        public Rental save(Rental rental) {
            savedRental = rental;
            return rental;
        }

        @Override
        public Optional<Rental> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public List<Rental> findAll() {
            return savedRental == null ? List.of() : List.of(savedRental);
        }
    }

    private static class FakeInventoryAssetAccessPort implements InventoryAssetAccessPort {
        private final boolean assetAvailable;

        private FakeInventoryAssetAccessPort(boolean assetAvailable) {
            this.assetAvailable = assetAvailable;
        }

        @Override
        public boolean isAssetAvailableForRental(UUID assetId) {
            return assetAvailable;
        }

        @Override
        public void markAssetAsDamaged(UUID assetId, String damageReport) {
        }

        @Override
        public List<AvailableAssetView> findAvailableAssetsByCategory(String category) {
            return List.of();
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
