package pl.pwr.miasi.equipmentrental.inventory.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pl.pwr.miasi.equipmentrental.inventory.application.dto.AvailableAssetDto;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.domain.Asset;
import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;
import pl.pwr.miasi.equipmentrental.inventory.domain.InventoryTag;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public class AssetRepositoryAdapter implements AssetRepository {

    private final AssetSpringDataRepository springDataRepository;

    public AssetRepositoryAdapter(AssetSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Asset save(Asset asset) {
        AssetJpaEntity entity = toEntity(asset);
        AssetJpaEntity savedEntity = springDataRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Asset> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByInventoryTag(InventoryTag inventoryTag) {
        return springDataRepository.existsByInventoryTag(inventoryTag.value());
    }

    @Override
    public List<Asset> findAll() {
        return springDataRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private AssetJpaEntity toEntity(Asset asset) {
        return new AssetJpaEntity(
                asset.getId(),
                asset.getEquipmentModelId(),
                asset.getInventoryTag().value(),
                asset.getCondition(),
                asset.getDamageReport()
        );
    }

    private Asset toDomain(AssetJpaEntity entity) {
        return new Asset(
                entity.getId(),
                entity.getEquipmentModelId(),
                new InventoryTag(entity.getInventoryTag()),
                entity.getCondition(),
                entity.getDamageReport()
        );
    }

    @Override
    public List<AvailableAssetDto> findOperationalByCategory(String category) {
        return springDataRepository.findOperationalAssetsByCategory(category, AssetCondition.OPERATIONAL)
                .stream()
                .map(view -> new AvailableAssetDto(
                        view.assetId(),
                        view.equipmentModelId(),
                        view.inventoryTag(),
                        view.modelName(),
                        view.category(),
                        view.manufacturer()
                ))
                .toList();
    }
}