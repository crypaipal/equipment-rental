package pl.pwr.miasi.equipmentrental.inventory.application.port.in;

import pl.pwr.miasi.equipmentrental.inventory.application.result.EquipmentModelResult;

import java.util.List;

public interface FindAllEquipmentModelsUseCase {

    List<EquipmentModelResult> findAll();
}