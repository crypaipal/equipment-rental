package pl.pwr.miasi.equipmentrental.inventory.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterAssetCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.dto.AvailableAssetDto;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.EquipmentModelRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;
import pl.pwr.miasi.equipmentrental.inventory.domain.Asset;
import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;
import pl.pwr.miasi.equipmentrental.inventory.domain.EquipmentModel;
import pl.pwr.miasi.equipmentrental.inventory.domain.InventoryTag;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.AssetRegisteredEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterAssetServiceTest {

    @Test
    void registersAssetAndPublishesAssetRegisteredEvent() {
        UUID equipmentModelId = UUID.randomUUID();
        FakeAssetRepository assetRepository = new FakeAssetRepository();
        FakeEquipmentModelRepository equipmentModelRepository = new FakeEquipmentModelRepository(equipmentModelId);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RegisterAssetService service = new RegisterAssetService(
                assetRepository,
                equipmentModelRepository,
                eventPublisher
        );

        AssetResult result = service.register(new RegisterAssetCommand(equipmentModelId, " INV-001 "));

        assertThat(assetRepository.savedAsset).isNotNull();
        assertThat(result.id()).isEqualTo(assetRepository.savedAsset.getId());
        assertThat(result.equipmentModelId()).isEqualTo(equipmentModelId);
        assertThat(result.inventoryTag()).isEqualTo("INV-001");
        assertThat(result.condition()).isEqualTo(AssetCondition.OPERATIONAL);
        assertThat(result.damageReport()).isNull();

        assertThat(eventPublisher.events).singleElement().isInstanceOf(AssetRegisteredEvent.class);
        AssetRegisteredEvent event = (AssetRegisteredEvent) eventPublisher.events.get(0);
        assertThat(event.assetId()).isEqualTo(result.id());
        assertThat(event.equipmentModelId()).isEqualTo(equipmentModelId);
        assertThat(event.inventoryTag()).isEqualTo("INV-001");
    }

    @Test
    void rejectsAssetRegistrationWhenEquipmentModelDoesNotExist() {
        FakeAssetRepository assetRepository = new FakeAssetRepository();
        FakeEquipmentModelRepository equipmentModelRepository = new FakeEquipmentModelRepository();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RegisterAssetService service = new RegisterAssetService(
                assetRepository,
                equipmentModelRepository,
                eventPublisher
        );

        assertThatThrownBy(() -> service.register(new RegisterAssetCommand(UUID.randomUUID(), "INV-001")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Equipment model not found");

        assertThat(assetRepository.savedAsset).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    @Test
    void rejectsDuplicateInventoryTagWithoutSavingOrPublishingEvent() {
        UUID equipmentModelId = UUID.randomUUID();
        FakeAssetRepository assetRepository = new FakeAssetRepository();
        assetRepository.inventoryTagExists = true;
        FakeEquipmentModelRepository equipmentModelRepository = new FakeEquipmentModelRepository(equipmentModelId);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RegisterAssetService service = new RegisterAssetService(
                assetRepository,
                equipmentModelRepository,
                eventPublisher
        );

        assertThatThrownBy(() -> service.register(new RegisterAssetCommand(equipmentModelId, "INV-001")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Asset with this inventory tag already exists");

        assertThat(assetRepository.savedAsset).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    private static class FakeAssetRepository implements AssetRepository {
        private boolean inventoryTagExists;
        private Asset savedAsset;

        @Override
        public Asset save(Asset asset) {
            savedAsset = asset;
            return asset;
        }

        @Override
        public Optional<Asset> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public boolean existsByInventoryTag(InventoryTag inventoryTag) {
            return inventoryTagExists;
        }

        @Override
        public List<Asset> findAll() {
            return List.of();
        }

        @Override
        public List<AvailableAssetDto> findOperationalByCategory(String category) {
            return Collections.emptyList();
        }


    }

    private static class FakeEquipmentModelRepository implements EquipmentModelRepository {
        private final UUID existingModelId;

        private FakeEquipmentModelRepository() {
            this(null);
        }

        private FakeEquipmentModelRepository(UUID existingModelId) {
            this.existingModelId = existingModelId;
        }

        @Override
        public EquipmentModel save(EquipmentModel equipmentModel) {
            return equipmentModel;
        }

        @Override
        public boolean existsByNameAndManufacturer(String name, String manufacturer) {
            return false;
        }

        @Override
        public boolean existsById(UUID id) {
            return existingModelId != null && existingModelId.equals(id);
        }

        @Override
        public List<EquipmentModel> findAll() {
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
