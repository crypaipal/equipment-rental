package pl.pwr.miasi.equipmentrental.inventory.application.port.out;

import pl.pwr.miasi.equipmentrental.inventory.application.dto.AvailableAssetDto;
import pl.pwr.miasi.equipmentrental.inventory.domain.Asset;
import pl.pwr.miasi.equipmentrental.inventory.domain.InventoryTag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository {

    Asset save(Asset asset);

    Optional<Asset> findById(UUID id);

    boolean existsByInventoryTag(InventoryTag inventoryTag);

    List<Asset> findAll();
    List<AvailableAssetDto> findOperationalByCategory(String category);
}