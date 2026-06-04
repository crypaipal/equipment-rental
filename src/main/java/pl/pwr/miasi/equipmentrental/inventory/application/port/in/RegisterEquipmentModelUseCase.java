package pl.pwr.miasi.equipmentrental.inventory.application.port.in;

import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterEquipmentModelCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.result.EquipmentModelResult;

public interface RegisterEquipmentModelUseCase {

    EquipmentModelResult register(RegisterEquipmentModelCommand command);
}