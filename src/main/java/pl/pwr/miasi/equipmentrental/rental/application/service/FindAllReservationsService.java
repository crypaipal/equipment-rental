package pl.pwr.miasi.equipmentrental.rental.application.service;

import pl.pwr.miasi.equipmentrental.rental.application.port.in.FindAllReservationsUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;

import java.util.List;

public class FindAllReservationsService implements FindAllReservationsUseCase {

    private final ReservationRepository reservationRepository;

    public FindAllReservationsService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<ReservationResult> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(reservation -> new ReservationResult(
                        reservation.getId(),
                        reservation.getUserId(),
                        reservation.getAssetId(),
                        reservation.getRentalPeriod().from(),
                        reservation.getRentalPeriod().to(),
                        reservation.getStatus(),
                        reservation.getRejectionReason(),
                        reservation.getCreatedAt()
                ))
                .toList();
    }
}