package pl.pwr.miasi.equipmentrental.rental.application.port.in;

import pl.pwr.miasi.equipmentrental.rental.application.command.ReviewReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;

public interface ReviewReservationUseCase {

    ReservationResult review(ReviewReservationCommand command);
}