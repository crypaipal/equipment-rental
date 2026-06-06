package pl.pwr.miasi.equipmentrental.rental.infrastructure.integration;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;

import java.util.UUID;

@Component
public class InventoryAssetAccessAdapter implements InventoryAssetAccessPort {

    private final AssetRepository assetRepository;

    public InventoryAssetAccessAdapter(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public boolean isAssetAvailableForRental(UUID assetId) {
        return assetRepository.findById(assetId)
                .map(asset -> asset.isAvailableForRental())
                .orElse(false);
    }
}