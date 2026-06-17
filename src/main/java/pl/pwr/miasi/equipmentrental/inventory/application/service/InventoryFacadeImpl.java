package pl.pwr.miasi.equipmentrental.inventory.application.service;

import org.springframework.stereotype.Service;
import pl.pwr.miasi.equipmentrental.inventory.application.dto.AvailableAssetDto;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.InventoryFacade;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryFacadeImpl implements InventoryFacade {

    private final AssetRepository assetRepository;

    public InventoryFacadeImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public boolean isAssetOperational(UUID assetId) {
        return assetRepository.findById(assetId)
                .map(asset -> asset.isAvailableForRental())
                .orElse(false);
    }

    @Override
    public List<AvailableAssetDto> findAvailableAssetsByCategory(String category) {
        return assetRepository.findOperationalByCategory(category);
    }
}