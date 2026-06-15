package pl.pwr.miasi.equipmentrental.inventory.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.inventory.application.command.ReportAssetDamageCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;
import pl.pwr.miasi.equipmentrental.inventory.domain.Asset;
import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;
import pl.pwr.miasi.equipmentrental.inventory.domain.InventoryTag;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.AssetDamagedEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportAssetDamageServiceTest {

    @Test
    void reportsAssetDamageAndPublishesDamagedEvent() {
        Asset asset = operationalAsset();
        FakeAssetRepository assetRepository = new FakeAssetRepository(asset);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ReportAssetDamageService service = new ReportAssetDamageService(assetRepository, eventPublisher);

        AssetResult result = service.reportAssetDamage(new ReportAssetDamageCommand(
                asset.getId(),
                "Cracked screen"
        ));

        assertThat(result.condition()).isEqualTo(AssetCondition.DAMAGED);
        assertThat(result.damageReport()).isEqualTo("Cracked screen");
        assertThat(assetRepository.savedAsset).isSameAs(asset);

        assertThat(eventPublisher.events).singleElement().isInstanceOf(AssetDamagedEvent.class);
        AssetDamagedEvent event = (AssetDamagedEvent) eventPublisher.events.get(0);
        assertThat(event.assetId()).isEqualTo(asset.getId());
        assertThat(event.inventoryTag()).isEqualTo("INV-010");
        assertThat(event.condition()).isEqualTo(AssetCondition.DAMAGED);
        assertThat(event.damageReport()).isEqualTo("Cracked screen");
    }

    @Test
    void rejectsDamageReportWhenAssetDoesNotExist() {
        FakeAssetRepository assetRepository = new FakeAssetRepository();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ReportAssetDamageService service = new ReportAssetDamageService(assetRepository, eventPublisher);

        assertThatThrownBy(() -> service.reportAssetDamage(new ReportAssetDamageCommand(
                UUID.randomUUID(),
                "Cracked screen"
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
                new InventoryTag("INV-010"),
                AssetCondition.OPERATIONAL,
                null
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
    }

    private static class RecordingEventPublisher implements EventPublisher {
        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }
    }
}
