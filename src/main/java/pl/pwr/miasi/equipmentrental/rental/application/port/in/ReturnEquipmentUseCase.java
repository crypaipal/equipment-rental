package pl.pwr.miasi.equipmentrental.rental.application.port.in;

import pl.pwr.miasi.equipmentrental.rental.application.command.ReturnEquipmentCommand;
import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;

public interface ReturnEquipmentUseCase {

    RentalResult returnEquipment(ReturnEquipmentCommand command);
}