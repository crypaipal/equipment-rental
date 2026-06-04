package pl.pwr.miasi.equipmentrental.inventory.application.service;

import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterEquipmentModelCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.RegisterEquipmentModelUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.EquipmentModelRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.result.EquipmentModelResult;
import pl.pwr.miasi.equipmentrental.inventory.domain.EquipmentModel;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.ModelRegisteredEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

public class RegisterEquipmentModelService implements RegisterEquipmentModelUseCase {

    private final EquipmentModelRepository equipmentModelRepository;
    private final EventPublisher eventPublisher;

    public RegisterEquipmentModelService(
            EquipmentModelRepository equipmentModelRepository,
            EventPublisher eventPublisher
    ) {
        this.equipmentModelRepository = equipmentModelRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EquipmentModelResult register(RegisterEquipmentModelCommand command) {
        if (equipmentModelRepository.existsByNameAndManufacturer(command.name(), command.manufacturer())) {
            throw new BusinessException("Equipment model with this name and manufacturer already exists");
        }

        EquipmentModel equipmentModel = EquipmentModel.create(
                command.name(),
                command.category(),
                command.manufacturer()
        );

        EquipmentModel savedModel = equipmentModelRepository.save(equipmentModel);

        eventPublisher.publish(ModelRegisteredEvent.create(
                savedModel.getId(),
                savedModel.getName(),
                savedModel.getCategory(),
                savedModel.getManufacturer()
        ));

        return new EquipmentModelResult(
                savedModel.getId(),
                savedModel.getName(),
                savedModel.getCategory(),
                savedModel.getManufacturer()
        );
    }
}