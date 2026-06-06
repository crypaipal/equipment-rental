package pl.pwr.miasi.equipmentrental.inventory.application.service;

import pl.pwr.miasi.equipmentrental.inventory.application.command.ChangeAssetConditionCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.ChangeAssetConditionUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;
import pl.pwr.miasi.equipmentrental.inventory.domain.Asset;
import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.AssetDamagedEvent;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.AssetRepairedEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

public class ChangeAssetConditionService implements ChangeAssetConditionUseCase {

    private final AssetRepository assetRepository;
    private final EventPublisher eventPublisher;

    public ChangeAssetConditionService(
            AssetRepository assetRepository,
            EventPublisher eventPublisher
    ) {
        this.assetRepository = assetRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AssetResult changeCondition(ChangeAssetConditionCommand command) {
        if (command.condition() == null) {
            throw new BusinessException("Asset condition cannot be null");
        }

        Asset asset = assetRepository.findById(command.assetId())
                .orElseThrow(() -> new NotFoundException("Asset not found"));

        if (command.condition() == AssetCondition.DAMAGED) {
            asset.markAsDamaged(command.damageReport());

            eventPublisher.publish(AssetDamagedEvent.create(
                    asset.getId(),
                    asset.getInventoryTag().value(),
                    asset.getCondition(),
                    asset.getDamageReport()
            ));
        } else if (command.condition() == AssetCondition.IN_REPAIR) {
            asset.sendToRepair(command.damageReport());

            eventPublisher.publish(AssetDamagedEvent.create(
                    asset.getId(),
                    asset.getInventoryTag().value(),
                    asset.getCondition(),
                    asset.getDamageReport()
            ));
        } else if (command.condition() == AssetCondition.OPERATIONAL) {
            asset.markAsRepaired();

            eventPublisher.publish(AssetRepairedEvent.create(
                    asset.getId(),
                    asset.getInventoryTag().value()
            ));
        } else {
            throw new BusinessException("Unsupported asset condition");
        }

        Asset savedAsset = assetRepository.save(asset);

        return new AssetResult(
                savedAsset.getId(),
                savedAsset.getEquipmentModelId(),
                savedAsset.getInventoryTag().value(),
                savedAsset.getCondition(),
                savedAsset.getDamageReport()
        );
    }
}