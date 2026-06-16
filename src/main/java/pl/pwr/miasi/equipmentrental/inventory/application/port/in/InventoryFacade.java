package pl.pwr.miasi.equipmentrental.inventory.application.port.in;

import org.springframework.stereotype.Service;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;
import pl.pwr.miasi.equipmentrental.inventory.infrastructure.persistence.AssetSpringDataRepository;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryFacade {

    private final AssetRepository assetRepository;
    private final AssetSpringDataRepository assetSpringDataRepository;

    public InventoryFacade(AssetRepository assetRepository, AssetSpringDataRepository assetSpringDataRepository) {
        this.assetRepository = assetRepository;
        this.assetSpringDataRepository = assetSpringDataRepository;
    }

    public boolean isAssetOperational(UUID assetId) {
        return assetRepository.findById(assetId)
                .map(asset -> asset.isAvailableForRental())
                .orElse(false);
    }

    public List<AvailableAssetDto> findAvailableAssetsByCategory(String category) {
        return assetSpringDataRepository.findOperationalAssetsByCategory(category, AssetCondition.OPERATIONAL)
                .stream()
                .map(asset -> new AvailableAssetDto(
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