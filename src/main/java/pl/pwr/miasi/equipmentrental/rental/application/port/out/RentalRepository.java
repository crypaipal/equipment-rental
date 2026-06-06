package pl.pwr.miasi.equipmentrental.rental.application.port.out;

import pl.pwr.miasi.equipmentrental.rental.domain.Rental;

import java.util.Optional;
import java.util.UUID;

public interface RentalRepository {

    Rental save(Rental rental);

    Optional<Rental> findById(UUID id);
}