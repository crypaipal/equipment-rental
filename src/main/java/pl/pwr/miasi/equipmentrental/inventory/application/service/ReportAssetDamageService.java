package pl.pwr.miasi.equipmentrental.inventory.application.service;

import pl.pwr.miasi.equipmentrental.inventory.application.command.ReportAssetDamageCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.ReportAssetDamageUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;
import pl.pwr.miasi.equipmentrental.inventory.domain.Asset;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.AssetDamagedEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

public class ReportAssetDamageService implements ReportAssetDamageUseCase {

    private final AssetRepository assetRepository;
    private final EventPublisher eventPublisher;

    public ReportAssetDamageService(
            AssetRepository assetRepository,
            EventPublisher eventPublisher
    ) {
        this.assetRepository = assetRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AssetResult reportAssetDamage(ReportAssetDamageCommand command) {
        Asset asset = assetRepository.findById(command.assetId())
                .orElseThrow(() -> new NotFoundException("Asset not found"));

        asset.markAsDamaged(command.damageReport());

        Asset savedAsset = assetRepository.save(asset);

        eventPublisher.publish(AssetDamagedEvent.create(
                savedAsset.getId(),
                savedAsset.getInventoryTag().value(),
                savedAsset.getCondition(),
                savedAsset.getDamageReport()
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