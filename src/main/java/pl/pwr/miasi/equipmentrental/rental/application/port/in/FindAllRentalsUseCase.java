package pl.pwr.miasi.equipmentrental.rental.application.port.in;

import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;

import java.util.List;

public interface FindAllRentalsUseCase {

    List<RentalResult> findAll();
}