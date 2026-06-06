package pl.pwr.miasi.equipmentrental.rental.application.port.in;

import pl.pwr.miasi.equipmentrental.rental.application.command.RequestReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;

public interface RequestReservationUseCase {

    ReservationResult request(RequestReservationCommand command);
}