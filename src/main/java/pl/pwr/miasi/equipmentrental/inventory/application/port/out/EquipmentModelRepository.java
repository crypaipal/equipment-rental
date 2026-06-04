package pl.pwr.miasi.equipmentrental.inventory.application.port.out;

import pl.pwr.miasi.equipmentrental.inventory.domain.EquipmentModel;

import java.util.UUID;

public interface EquipmentModelRepository {

    EquipmentModel save(EquipmentModel equipmentModel);

    boolean existsByNameAndManufacturer(String name, String manufacturer);

    boolean existsById(UUID id);
}