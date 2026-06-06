package pl.pwr.miasi.equipmentrental.rental.application.port.in;

import pl.pwr.miasi.equipmentrental.rental.application.command.CancelReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;

public interface CancelReservationUseCase {

    ReservationResult cancel(CancelReservationCommand command);
}