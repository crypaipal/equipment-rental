package pl.pwr.miasi.equipmentrental.inventory.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.inventory.application.command.ChangeAssetConditionCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.dto.AvailableAssetDto;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;
import pl.pwr.miasi.equipmentrental.inventory.domain.Asset;
import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;
import pl.pwr.miasi.equipmentrental.inventory.domain.InventoryTag;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.AssetDamagedEvent;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.AssetRepairedEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeAssetConditionServiceTest {

    @Test
    void changesAssetToDamagedAndPublishesDamagedEvent() {
        Asset asset = operationalAsset();
        FakeAssetRepository assetRepository = new FakeAssetRepository(asset);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ChangeAssetConditionService service = new ChangeAssetConditionService(assetRepository, eventPublisher);

        AssetResult result = service.changeCondition(new ChangeAssetConditionCommand(
                asset.getId(),
                AssetCondition.DAMAGED,
                "Broken lens"
        ));

        assertThat(result.condition()).isEqualTo(AssetCondition.DAMAGED);
        assertThat(result.damageReport()).isEqualTo("Broken lens");
        assertThat(assetRepository.savedAsset).isSameAs(asset);

        assertThat(eventPublisher.events).singleElement().isInstanceOf(AssetDamagedEvent.class);
        AssetDamagedEvent event = (AssetDamagedEvent) eventPublisher.events.get(0);
        assertThat(event.assetId()).isEqualTo(asset.getId());
        assertThat(event.inventoryTag()).isEqualTo("INV-001");
        assertThat(event.condition()).isEqualTo(AssetCondition.DAMAGED);
        assertThat(event.damageReport()).isEqualTo("Broken lens");
    }

    @Test
    void changesAssetToInRepairAndPublishesDamagedEventWithInRepairCondition() {
        Asset asset = operationalAsset();
        FakeAssetRepository assetRepository = new FakeAssetRepository(asset);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ChangeAssetConditionService service = new ChangeAssetConditionService(assetRepository, eventPublisher);

        AssetResult result = service.changeCondition(new ChangeAssetConditionCommand(
                asset.getId(),
                AssetCondition.IN_REPAIR,
                "Sent to service"
        ));

        assertThat(result.condition()).isEqualTo(AssetCondition.IN_REPAIR);
        assertThat(result.damageReport()).isEqualTo("Sent to service");
        assertThat(assetRepository.savedAsset).isSameAs(asset);

        assertThat(eventPublisher.events).singleElement().isInstanceOf(AssetDamagedEvent.class);
        AssetDamagedEvent event = (AssetDamagedEvent) eventPublisher.events.get(0);
        assertThat(event.assetId()).isEqualTo(asset.getId());
        assertThat(event.inventoryTag()).isEqualTo("INV-001");
        assertThat(event.condition()).isEqualTo(AssetCondition.IN_REPAIR);
        assertThat(event.damageReport()).isEqualTo("Sent to service");
    }

    @Test
    void changesAssetToOperationalAndPublishesRepairedEvent() {
        Asset asset = damagedAsset();
        FakeAssetRepository assetRepository = new FakeAssetRepository(asset);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ChangeAssetConditionService service = new ChangeAssetConditionService(assetRepository, eventPublisher);

        AssetResult result = service.changeCondition(new ChangeAssetConditionCommand(
                asset.getId(),
                AssetCondition.OPERATIONAL,
                null
        ));

        assertThat(result.condition()).isEqualTo(AssetCondition.OPERATIONAL);
        assertThat(result.damageReport()).isNull();
        assertThat(assetRepository.savedAsset).isSameAs(asset);

        assertThat(eventPublisher.events).singleElement().isInstanceOf(AssetRepairedEvent.class);
        AssetRepairedEvent event = (AssetRepairedEvent) eventPublisher.events.get(0);
        assertThat(event.assetId()).isEqualTo(asset.getId());
        assertThat(event.inventoryTag()).isEqualTo("INV-001");
    }

    @Test
    void rejectsNullConditionWithoutSavingOrPublishingEvent() {
        Asset asset = operationalAsset();
        FakeAssetRepository assetRepository = new FakeAssetRepository(asset);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ChangeAssetConditionService service = new ChangeAssetConditionService(assetRepository, eventPublisher);

        assertThatThrownBy(() -> service.changeCondition(new ChangeAssetConditionCommand(
                asset.getId(),
                null,
                "Broken lens"
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Asset condition cannot be null");

        assertThat(assetRepository.savedAsset).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    @Test
    void rejectsConditionChangeWhenAssetDoesNotExist() {
        FakeAssetRepository assetRepository = new FakeAssetRepository();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ChangeAssetConditionService service = new ChangeAssetConditionService(assetRepository, eventPublisher);

        assertThatThrownBy(() -> service.changeCondition(new ChangeAssetConditionCommand(
                UUID.randomUUID(),
                AssetCondition.DAMAGED,
                "Broken lens"
        )))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Asset not found");

        assertThat(assetRepository.savedAsset).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    private static Asset operationalAsset() {
        return new Asset(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new InventoryTag("INV-001"),
                AssetCondition.OPERATIONAL,
                null
        );
    }

    private static Asset damagedAsset() {
        return new Asset(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new InventoryTag("INV-001"),
                AssetCondition.DAMAGED,
                "Broken lens"
        );
    }

    private static class FakeAssetRepository implements AssetRepository {
        private final Asset asset;
        private Asset savedAsset;

        private FakeAssetRepository() {
            this(null);
        }

        private FakeAssetRepository(Asset asset) {
            this.asset = asset;
        }

        @Override
        public Asset save(Asset asset) {
            savedAsset = asset;
            return asset;
        }

        @Override
        public Optional<Asset> findById(UUID id) {
            if (asset != null && asset.getId().equals(id)) {
                return Optional.of(asset);
            }

            return Optional.empty();
        }

        @Override
        public boolean existsByInventoryTag(InventoryTag inventoryTag) {
            return false;
        }

        @Override
        public List<Asset> findAll() {
            return asset == null ? List.of() : List.of(asset);
        }

        @Override
        public List<AvailableAssetDto> findOperationalByCategory(String category) {
            return Collections.emptyList();
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
