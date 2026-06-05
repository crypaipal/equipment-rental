package pl.pwr.miasi.equipmentrental.inventory.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterAssetCommand;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterAssetServiceTest {

    @Test
    void registersAssetForExistingEquipmentModelAndPublishesAssetRegisteredEvent() {
        UUID equipmentModelId = UUID.randomUUID();
        FakeAssetRepository assetRepository = new FakeAssetRepository();
        FakeEquipmentModelRepository equipmentModelRepository = new FakeEquipmentModelRepository();
        equipmentModelRepository.existingModelId = equipmentModelId;
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RegisterAssetService service = new RegisterAssetService(
                assetRepository,
                equipmentModelRepository,
                eventPublisher
        );

        AssetResult result = service.register(new RegisterAssetCommand(equipmentModelId, "CAM-001"));

        assertThat(result.id()).isNotNull();
        assertThat(result.equipmentModelId()).isEqualTo(equipmentModelId);
        assertThat(result.inventoryTag()).isEqualTo("CAM-001");
        assertThat(result.condition()).isEqualTo(AssetCondition.OPERATIONAL);
        assertThat(assetRepository.savedAsset).isNotNull();
        assertThat(eventPublisher.publishedEvents)
                .singleElement()
                .isInstanceOf(AssetRegisteredEvent.class);
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

        assertThatThrownBy(() -> service.register(new RegisterAssetCommand(UUID.randomUUID(), "CAM-001")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Equipment model not found");

        assertThat(assetRepository.savedAsset).isNull();
        assertThat(eventPublisher.publishedEvents).isEmpty();
    }

    @Test
    void rejectsAssetRegistrationWithDuplicateInventoryTag() {
        UUID equipmentModelId = UUID.randomUUID();
        FakeAssetRepository assetRepository = new FakeAssetRepository();
        assetRepository.duplicateInventoryTag = true;
        FakeEquipmentModelRepository equipmentModelRepository = new FakeEquipmentModelRepository();
        equipmentModelRepository.existingModelId = equipmentModelId;
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RegisterAssetService service = new RegisterAssetService(
                assetRepository,
                equipmentModelRepository,
                eventPublisher
        );

        assertThatThrownBy(() -> service.register(new RegisterAssetCommand(equipmentModelId, "CAM-001")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Asset with this inventory tag already exists");

        assertThat(assetRepository.savedAsset).isNull();
        assertThat(eventPublisher.publishedEvents).isEmpty();
    }

    private static final class FakeAssetRepository implements AssetRepository {
        private boolean duplicateInventoryTag;
        private Asset savedAsset;

        @Override
        public Asset save(Asset asset) {
            savedAsset = asset;
            return asset;
        }

        @Override
        public boolean existsByInventoryTag(InventoryTag inventoryTag) {
            return duplicateInventoryTag;
        }
    }

    private static final class FakeEquipmentModelRepository implements EquipmentModelRepository {
        private UUID existingModelId;

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
    }

    private static final class RecordingEventPublisher implements EventPublisher {
        private final List<DomainEvent> publishedEvents = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            publishedEvents.add(event);
        }
    }
}
