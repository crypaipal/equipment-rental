package pl.pwr.miasi.equipmentrental.rental.application.port.out;

import pl.pwr.miasi.equipmentrental.rental.domain.RentalPeriod;
import pl.pwr.miasi.equipmentrental.rental.domain.Reservation;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(UUID id);

    boolean existsActiveReservationForAsset(UUID assetId, RentalPeriod rentalPeriod);
}