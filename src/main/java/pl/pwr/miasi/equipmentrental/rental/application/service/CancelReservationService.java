package pl.pwr.miasi.equipmentrental.rental.application.service;

import pl.pwr.miasi.equipmentrental.rental.application.command.CancelReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.CancelReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;
import pl.pwr.miasi.equipmentrental.rental.domain.Reservation;
import pl.pwr.miasi.equipmentrental.rental.domain.events.ReservationCancelledEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

public class CancelReservationService implements CancelReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final EventPublisher eventPublisher;

    public CancelReservationService(
            ReservationRepository reservationRepository,
            EventPublisher eventPublisher
    ) {
        this.reservationRepository = reservationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ReservationResult cancel(CancelReservationCommand command) {
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        reservation.cancel();

        Reservation savedReservation = reservationRepository.save(reservation);

        eventPublisher.publish(ReservationCancelledEvent.create(
                savedReservation.getId(),
                savedReservation.getUserId(),
                savedReservation.getAssetId(),
                savedReservation.getRentalPeriod().from(),
                savedReservation.getRentalPeriod().to()
        ));

        return new ReservationResult(
                savedReservation.getId(),
                savedReservation.getUserId(),
                savedReservation.getAssetId(),
                savedReservation.getRentalPeriod().from(),
                savedReservation.getRentalPeriod().to(),
                savedReservation.getStatus(),
                savedReservation.getRejectionReason(),
                savedReservation.getCreatedAt()
        );
    }
}