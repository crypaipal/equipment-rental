package pl.pwr.miasi.equipmentrental.rental.infrastructure.integration;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.domain.Asset;
import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.AssetDamagedEvent;
import pl.pwr.miasi.equipmentrental.inventory.infrastructure.persistence.AssetSpringDataRepository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.AvailableAssetView;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

@Component
public class InventoryAssetAccessAdapter implements InventoryAssetAccessPort {

    private final AssetRepository assetRepository;
    private final AssetSpringDataRepository assetSpringDataRepository;
    private final EventPublisher eventPublisher;

    public InventoryAssetAccessAdapter(
            AssetRepository assetRepository,
            AssetSpringDataRepository assetSpringDataRepository,
            EventPublisher eventPublisher
    ) {
        this.assetRepository = assetRepository;
        this.assetSpringDataRepository = assetSpringDataRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean isAssetAvailableForRental(UUID assetId) {
        return assetRepository.findById(assetId)
                .map(Asset::isAvailableForRental)
                .orElse(false);
    }

    @Override
    public void markAssetAsDamaged(UUID assetId, String damageReport) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Asset not found"));

        asset.markAsDamaged(damageReport);
        Asset savedAsset = assetRepository.save(asset);

        eventPublisher.publish(AssetDamagedEvent.create(
                savedAsset.getId(),
                savedAsset.getInventoryTag().value(),
                savedAsset.getCondition(),
                savedAsset.getDamageReport()
        ));
    }

    @Override
    public List<AvailableAssetView> findAvailableAssetsByCategory(String category) {
        return assetSpringDataRepository.findOperationalAssetsByCategory(
                        category,
                        AssetCondition.OPERATIONAL
                )
                .stream()
                .map(asset -> new AvailableAssetView(
                        asset.assetId(),
                        asset.equipmentModelId(),
                        asset.inventoryTag(),
                        asset.modelName(),
                        asset.category(),
                        asset.manufacturer()
                ))
                .toList();
    }
}