package pl.pwr.miasi.equipmentrental.rental.application.port.out;

import pl.pwr.miasi.equipmentrental.rental.domain.Rental;

public interface RentalRepository {

    Rental save(Rental rental);
}