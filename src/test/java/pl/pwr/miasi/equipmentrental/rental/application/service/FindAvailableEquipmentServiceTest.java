package pl.pwr.miasi.equipmentrental.rental.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.rental.application.command.FindAvailableEquipmentQuery;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.AvailableAssetView;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.AvailableAssetResult;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalPeriod;
import pl.pwr.miasi.equipmentrental.rental.domain.Reservation;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FindAvailableEquipmentServiceTest {

    private static final Instant PERIOD_FROM = Instant.parse("2026-06-15T10:00:00Z");
    private static final Instant PERIOD_TO = Instant.parse("2026-06-15T12:00:00Z");

    @Test
    void returnsOnlyAssetsThatAreAvailableAndNotReservedInSelectedPeriod() {
        UUID availableAssetId = UUID.randomUUID();
        UUID reservedAssetId = UUID.randomUUID();
        UUID equipmentModelId = UUID.randomUUID();
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(List.of(
                new AvailableAssetView(
                        availableAssetId,
                        equipmentModelId,
                        "INV-001",
                        "A7 III",
                        "Camera",
                        "Sony"
                ),
                new AvailableAssetView(
                        reservedAssetId,
                        UUID.randomUUID(),
                        "INV-002",
                        "EOS R6",
                        "Camera",
                        "Canon"
                )
        ));
        FakeReservationRepository reservationRepository = new FakeReservationRepository(List.of(reservedAssetId));
        FindAvailableEquipmentService service = new FindAvailableEquipmentService(
                inventoryAssetAccessPort,
                reservationRepository
        );

        List<AvailableAssetResult> result = service.findAvailable(query("Camera"));

        assertThat(result).singleElement().satisfies(asset -> {
            assertThat(asset.assetId()).isEqualTo(availableAssetId);
            assertThat(asset.equipmentModelId()).isEqualTo(equipmentModelId);
            assertThat(asset.inventoryTag()).isEqualTo("INV-001");
            assertThat(asset.modelName()).isEqualTo("A7 III");
            assertThat(asset.category()).isEqualTo("Camera");
            assertThat(asset.manufacturer()).isEqualTo("Sony");
        });
        assertThat(inventoryAssetAccessPort.requestedCategory).isEqualTo("Camera");
        assertThat(reservationRepository.checkedRentalPeriod).isEqualTo(new RentalPeriod(PERIOD_FROM, PERIOD_TO));
    }

    @Test
    void returnsEmptyListWhenAllAvailableAssetsAreReservedInSelectedPeriod() {
        UUID reservedAssetId = UUID.randomUUID();
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(List.of(
                new AvailableAssetView(
                        reservedAssetId,
                        UUID.randomUUID(),
                        "INV-010",
                        "ThinkPad T14",
                        "Laptop",
                        "Lenovo"
                )
        ));
        FakeReservationRepository reservationRepository = new FakeReservationRepository(List.of(reservedAssetId));
        FindAvailableEquipmentService service = new FindAvailableEquipmentService(
                inventoryAssetAccessPort,
                reservationRepository
        );

        List<AvailableAssetResult> result = service.findAvailable(query("Laptop"));

        assertThat(result).isEmpty();
        assertThat(inventoryAssetAccessPort.requestedCategory).isEqualTo("Laptop");
    }

    @Test
    void rejectsBlankCategory() {
        FakeInventoryAssetAccessPort inventoryAssetAccessPort = new FakeInventoryAssetAccessPort(List.of());
        FakeReservationRepository reservationRepository = new FakeReservationRepository(List.of());
        FindAvailableEquipmentService service = new FindAvailableEquipmentService(
                inventoryAssetAccessPort,
                reservationRepository
        );

        assertThatThrownBy(() -> service.findAvailable(query(" ")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Category cannot be empty");

        assertThat(inventoryAssetAccessPort.requestedCategory).isNull();
        assertThat(reservationRepository.checkedRentalPeriod).isNull();
    }

    private static FindAvailableEquipmentQuery query(String category) {
        return new FindAvailableEquipmentQuery(category, PERIOD_FROM, PERIOD_TO);
    }

    private static class FakeInventoryAssetAccessPort implements InventoryAssetAccessPort {
        private final List<AvailableAssetView> assets;
        private String requestedCategory;

        private FakeInventoryAssetAccessPort(List<AvailableAssetView> assets) {
            this.assets = assets;
        }

        @Override
        public boolean isAssetAvailableForRental(UUID assetId) {
            return false;
        }



        @Override
        public List<AvailableAssetView> findAvailableAssetsByCategory(String category) {
            requestedCategory = category;
            return assets;
        }
    }

    private static class FakeReservationRepository implements ReservationRepository {
        private final List<UUID> reservedAssetIds;
        private RentalPeriod checkedRentalPeriod;

        private FakeReservationRepository(List<UUID> reservedAssetIds) {
            this.reservedAssetIds = reservedAssetIds;
        }

        @Override
        public Reservation save(Reservation reservation) {
            return reservation;
        }

        @Override
        public Optional<Reservation> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public boolean existsActiveReservationForAsset(UUID assetId, RentalPeriod rentalPeriod) {
            return false;
        }

        @Override
        public List<UUID> findReservedAssetIds(RentalPeriod rentalPeriod) {
            checkedRentalPeriod = rentalPeriod;
            return reservedAssetIds;
        }

        @Override
        public List<Reservation> findAll() {
            return List.of();
        }
    }
}
