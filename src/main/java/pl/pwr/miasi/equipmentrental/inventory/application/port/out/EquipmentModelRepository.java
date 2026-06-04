package pl.pwr.miasi.equipmentrental.inventory.application.port.out;

import pl.pwr.miasi.equipmentrental.inventory.domain.EquipmentModel;

public interface EquipmentModelRepository {

    EquipmentModel save(EquipmentModel equipmentModel);

    boolean existsByNameAndManufacturer(String name, String manufacturer);
}