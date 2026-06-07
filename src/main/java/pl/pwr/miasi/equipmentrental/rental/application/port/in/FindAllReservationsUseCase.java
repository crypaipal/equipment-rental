package pl.pwr.miasi.equipmentrental.rental.application.port.in;

import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;

import java.util.List;

public interface FindAllReservationsUseCase {

    List<ReservationResult> findAll();
}