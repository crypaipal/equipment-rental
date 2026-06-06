package pl.pwr.miasi.equipmentrental.rental.application.port.out;

import java.util.List;
import java.util.UUID;

public interface InventoryAssetAccessPort {

    boolean isAssetAvailableForRental(UUID assetId);

    void markAssetAsDamaged(UUID assetId, String damageReport);

    List<AvailableAssetView> findAvailableAssetsByCategory(String category);
}