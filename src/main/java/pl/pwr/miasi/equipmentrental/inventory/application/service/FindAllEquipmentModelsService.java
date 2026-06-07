package pl.pwr.miasi.equipmentrental.inventory.application.service;

import pl.pwr.miasi.equipmentrental.inventory.application.port.in.FindAllEquipmentModelsUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.EquipmentModelRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.result.EquipmentModelResult;

import java.util.List;

public class FindAllEquipmentModelsService implements FindAllEquipmentModelsUseCase {

    private final EquipmentModelRepository equipmentModelRepository;

    public FindAllEquipmentModelsService(EquipmentModelRepository equipmentModelRepository) {
        this.equipmentModelRepository = equipmentModelRepository;
    }

    @Override
    public List<EquipmentModelResult> findAll() {
        return equipmentModelRepository.findAll()
                .stream()
                .map(model -> new EquipmentModelResult(
                        model.getId(),
                        model.getName(),
                        model.getCategory(),
                        model.getManufacturer()
                ))
                .toList();
    }
}