package pl.pwr.miasi.equipmentrental.rental.application.port.in;

import pl.pwr.miasi.equipmentrental.rental.application.command.CheckoutEquipmentCommand;
import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;

public interface CheckoutEquipmentUseCase {

    RentalResult checkout(CheckoutEquipmentCommand command);
}