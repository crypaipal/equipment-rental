package pl.pwr.miasi.equipmentrental.rental.application.port.out;

import java.util.UUID;

public interface InventoryAssetAccessPort {

    boolean isAssetAvailableForRental(UUID assetId);
}