package pl.pwr.miasi.equipmentrental.inventory.application.port.out;

import pl.pwr.miasi.equipmentrental.inventory.domain.Asset;
import pl.pwr.miasi.equipmentrental.inventory.domain.InventoryTag;

public interface AssetRepository {

    Asset save(Asset asset);

    boolean existsByInventoryTag(InventoryTag inventoryTag);
}