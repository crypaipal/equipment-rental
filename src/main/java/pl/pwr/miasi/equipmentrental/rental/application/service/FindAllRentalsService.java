package pl.pwr.miasi.equipmentrental.rental.application.service;

import pl.pwr.miasi.equipmentrental.rental.application.port.in.FindAllRentalsUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.RentalRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;

import java.util.List;

public class FindAllRentalsService implements FindAllRentalsUseCase {

    private final RentalRepository rentalRepository;

    public FindAllRentalsService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    @Override
    public List<RentalResult> findAll() {
        return rentalRepository.findAll()
                .stream()
                .map(rental -> new RentalResult(
                        rental.getId(),
                        rental.getReservationId(),
                        rental.getUserId(),
                        rental.getAssetId(),
                        rental.getCheckoutAt(),
                        rental.getExpectedReturnAt(),
                        rental.getReturnedAt(),
                        rental.getStatus()
                ))
                .toList();
    }
}