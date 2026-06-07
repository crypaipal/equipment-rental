package pl.pwr.miasi.equipmentrental.inventory.application.service;

import pl.pwr.miasi.equipmentrental.inventory.application.port.in.FindAllAssetsUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;

import java.util.List;

public class FindAllAssetsService implements FindAllAssetsUseCase {

    private final AssetRepository assetRepository;

    public FindAllAssetsService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public List<AssetResult> findAll() {
        return assetRepository.findAll()
                .stream()
                .map(asset -> new AssetResult(
                        asset.getId(),
                        asset.getEquipmentModelId(),
                        asset.getInventoryTag().value(),
                        asset.getCondition(),
                        asset.getDamageReport()
                ))
                .toList();
    }
}