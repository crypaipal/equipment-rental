package pl.pwr.miasi.equipmentrental.rental.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.rental.application.command.RequestReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.AvailableAssetView;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.UserAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalPeriod;
import pl.pwr.miasi.equipmentrental.rental.domain.Reservation;
import pl.pwr.miasi.equipmentrental.rental.domain.ReservationStatus;
import pl.pwr.miasi.equipmentrental.rental.domain.events.ReservationRequestedEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestReservationServiceTest {

    private static final Instant PERIOD_FROM = Instant.parse("2026-06-15T10:00:00Z");
    private static final Instant PERIOD_TO = Instant.parse("2026-06-15T12:00:00Z");

    @Test
    void requestsReservationForEligibleUserAndAvailableAsset() {
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        FakeUserAccessPort userAccessPort = new FakeUserAccessPort(true);
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(true);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RequestReservationService service = new RequestReservationService(
                reservationRepository,
                userAccessPort,
                inventoryAssetAccessPort,
                eventPublisher
        );

        ReservationResult result = service.request(command(userId, assetId));

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.assetId()).isEqualTo(assetId);
        assertThat(result.periodFrom()).isEqualTo(PERIOD_FROM);
        assertThat(result.periodTo()).isEqualTo(PERIOD_TO);
        assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
        assertThat(result.rejectionReason()).isNull();
        assertThat(reservationRepository.savedReservation).isNotNull();

        assertThat(eventPublisher.events).singleElement().isInstanceOf(ReservationRequestedEvent.class);
        ReservationRequestedEvent event = (ReservationRequestedEvent) eventPublisher.events.get(0);
        assertThat(event.reservationId()).isEqualTo(result.id());
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.assetId()).isEqualTo(assetId);
        assertThat(event.periodFrom()).isEqualTo(PERIOD_FROM);
        assertThat(event.periodTo()).isEqualTo(PERIOD_TO);
    }

    @Test
    void rejectsReservationWhenUserCannotRent() {
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        FakeUserAccessPort userAccessPort = new FakeUserAccessPort(false);
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(true);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RequestReservationService service = new RequestReservationService(
                reservationRepository,
                userAccessPort,
                inventoryAssetAccessPort,
                eventPublisher
        );

        assertThatThrownBy(() -> service.request(command(userId, assetId)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("User is not allowed to rent equipment");

        assertThat(reservationRepository.savedReservation).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    @Test
    void rejectsReservationWhenAssetIsNotAvailableForRental() {
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        FakeUserAccessPort userAccessPort = new FakeUserAccessPort(true);
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(false);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RequestReservationService service = new RequestReservationService(
                reservationRepository,
                userAccessPort,
                inventoryAssetAccessPort,
                eventPublisher
        );

        assertThatThrownBy(() -> service.request(command(userId, assetId)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Asset is not available for rental");

        assertThat(reservationRepository.savedReservation).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    @Test
    void rejectsReservationWhenAssetIsAlreadyReservedInSelectedPeriod() {
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        reservationRepository.activeReservationExists = true;
        FakeUserAccessPort userAccessPort = new FakeUserAccessPort(true);
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(true);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RequestReservationService service = new RequestReservationService(
                reservationRepository,
                userAccessPort,
                inventoryAssetAccessPort,
                eventPublisher
        );

        assertThatThrownBy(() -> service.request(command(userId, assetId)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Asset is already reserved in selected period");

        assertThat(reservationRepository.checkedAssetId).isEqualTo(assetId);
        assertThat(reservationRepository.checkedRentalPeriod).isEqualTo(new RentalPeriod(PERIOD_FROM, PERIOD_TO));
        assertThat(reservationRepository.savedReservation).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    private static RequestReservationCommand command(UUID userId, UUID assetId) {
        return new RequestReservationCommand(userId, assetId, PERIOD_FROM, PERIOD_TO);
    }

    private static class FakeReservationRepository implements ReservationRepository {
        private boolean activeReservationExists;
        private UUID checkedAssetId;
        private RentalPeriod checkedRentalPeriod;
        private Reservation savedReservation;

        @Override
        public Reservation save(Reservation reservation) {
            savedReservation = reservation;
            return reservation;
        }

        @Override
        public Optional<Reservation> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public boolean existsActiveReservationForAsset(UUID assetId, RentalPeriod rentalPeriod) {
            checkedAssetId = assetId;
            checkedRentalPeriod = rentalPeriod;
            return activeReservationExists;
        }

        @Override
        public List<UUID> findReservedAssetIds(RentalPeriod rentalPeriod) {
            return List.of();
        }

        @Override
        public List<Reservation> findAll() {
            return List.of();
        }
    }

    private static class FakeUserAccessPort implements UserAccessPort {
        private final boolean canRent;

        private FakeUserAccessPort(boolean canRent) {
            this.canRent = canRent;
        }

        @Override
        public boolean canUserRent(UUID userId) {
            return canRent;
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
