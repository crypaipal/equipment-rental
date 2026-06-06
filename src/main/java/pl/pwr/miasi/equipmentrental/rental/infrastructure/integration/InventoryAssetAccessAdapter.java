package pl.pwr.miasi.equipmentrental.rental.infrastructure.integration;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.domain.Asset;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.AssetDamagedEvent;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

import java.util.UUID;

@Component
public class InventoryAssetAccessAdapter implements InventoryAssetAccessPort {

    private final AssetRepository assetRepository;
    private final EventPublisher eventPublisher;

    public InventoryAssetAccessAdapter(
            AssetRepository assetRepository,
            EventPublisher eventPublisher
    ) {
        this.assetRepository = assetRepository;
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
}