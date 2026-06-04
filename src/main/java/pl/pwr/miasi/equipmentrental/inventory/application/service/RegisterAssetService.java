package pl.pwr.miasi.equipmentrental.inventory.application.service;

import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterAssetCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.RegisterAssetUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.EquipmentModelRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;
import pl.pwr.miasi.equipmentrental.inventory.domain.Asset;
import pl.pwr.miasi.equipmentrental.inventory.domain.InventoryTag;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.AssetRegisteredEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

public class RegisterAssetService implements RegisterAssetUseCase {

    private final AssetRepository assetRepository;
    private final EquipmentModelRepository equipmentModelRepository;
    private final EventPublisher eventPublisher;

    public RegisterAssetService(
            AssetRepository assetRepository,
            EquipmentModelRepository equipmentModelRepository,
            EventPublisher eventPublisher
    ) {
        this.assetRepository = assetRepository;
        this.equipmentModelRepository = equipmentModelRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AssetResult register(RegisterAssetCommand command) {
        if (!equipmentModelRepository.existsById(command.equipmentModelId())) {
            throw new NotFoundException("Equipment model not found");
        }

        InventoryTag inventoryTag = new InventoryTag(command.inventoryTag());

        if (assetRepository.existsByInventoryTag(inventoryTag)) {
            throw new BusinessException("Asset with this inventory tag already exists");
        }

        Asset asset = Asset.register(command.equipmentModelId(), inventoryTag);
        Asset savedAsset = assetRepository.save(asset);

        eventPublisher.publish(AssetRegisteredEvent.create(
                savedAsset.getId(),
                savedAsset.getEquipmentModelId(),
                savedAsset.getInventoryTag().value()
        ));

        return new AssetResult(
                savedAsset.getId(),
                savedAsset.getEquipmentModelId(),
                savedAsset.getInventoryTag().value(),
                savedAsset.getCondition(),
                savedAsset.getDamageReport()
        );
    }
}