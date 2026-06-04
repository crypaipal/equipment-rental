package pl.pwr.miasi.equipmentrental.inventory.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.EquipmentModelRepository;
import pl.pwr.miasi.equipmentrental.inventory.domain.EquipmentModel;

import java.util.UUID;

@Repository
public class EquipmentModelRepositoryAdapter implements EquipmentModelRepository {

    private final EquipmentModelSpringDataRepository springDataRepository;

    public EquipmentModelRepositoryAdapter(EquipmentModelSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public EquipmentModel save(EquipmentModel equipmentModel) {
        EquipmentModelJpaEntity entity = toEntity(equipmentModel);
        EquipmentModelJpaEntity savedEntity = springDataRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public boolean existsByNameAndManufacturer(String name, String manufacturer) {
        return springDataRepository.existsByNameAndManufacturer(name, manufacturer);
    }

    private EquipmentModelJpaEntity toEntity(EquipmentModel equipmentModel) {
        return new EquipmentModelJpaEntity(
                equipmentModel.getId(),
                equipmentModel.getName(),
                equipmentModel.getCategory(),
                equipmentModel.getManufacturer()
        );
    }

    private EquipmentModel toDomain(EquipmentModelJpaEntity entity) {
        return new EquipmentModel(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getManufacturer()
        );
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepository.existsById(id);
    }
}