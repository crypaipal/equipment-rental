package pl.pwr.miasi.equipmentrental.rental.infrastructure.integration;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.AvailableAssetDto;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.InventoryFacade;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.AvailableAssetView;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;

import java.util.List;
import java.util.UUID;

@Component
public class InventoryAssetAccessAdapter implements InventoryAssetAccessPort {

    private final InventoryFacade inventoryFacade;

    public InventoryAssetAccessAdapter(InventoryFacade inventoryFacade) {
        this.inventoryFacade = inventoryFacade;
    }

    @Override
    public boolean isAssetAvailableForRental(UUID assetId) {
        return inventoryFacade.isAssetOperational(assetId);
    }

    @Override
    public List<AvailableAssetView> findAvailableAssetsByCategory(String category) {
        return inventoryFacade.findAvailableAssetsByCategory(category)
                .stream()
                .map(dto -> new AvailableAssetView(
                        dto.assetId(),
                        dto.equipmentModelId(),
                        dto.inventoryTag(),
                        dto.modelName(),
                        dto.category(),
                        dto.manufacturer()
                ))
                .toList();
    }
}