package pl.pwr.miasi.equipmentrental.inventory.application.port.in;

import pl.pwr.miasi.equipmentrental.inventory.application.dto.AvailableAssetDto;

import java.util.List;
import java.util.UUID;

public interface InventoryFacade {
    boolean isAssetOperational(UUID assetId);
    List<AvailableAssetDto> findAvailableAssetsByCategory(String category);
}