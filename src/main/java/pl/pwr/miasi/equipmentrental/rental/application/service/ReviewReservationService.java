package pl.pwr.miasi.equipmentrental.rental.application.service;

import pl.pwr.miasi.equipmentrental.rental.application.command.ReviewReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.ReviewReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;
import pl.pwr.miasi.equipmentrental.rental.domain.Reservation;
import pl.pwr.miasi.equipmentrental.rental.domain.events.ReservationApprovedEvent;
import pl.pwr.miasi.equipmentrental.rental.domain.events.ReservationRejectedEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

public class ReviewReservationService implements ReviewReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final EventPublisher eventPublisher;

    public ReviewReservationService(
            ReservationRepository reservationRepository,
            EventPublisher eventPublisher
    ) {
        this.reservationRepository = reservationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ReservationResult review(ReviewReservationCommand command) {
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        if (command.approved()) {
            reservation.approve();
        } else {
            reservation.reject(command.rejectionReason());
        }

        Reservation savedReservation = reservationRepository.save(reservation);

        if (command.approved()) {
            eventPublisher.publish(ReservationApprovedEvent.create(
                    savedReservation.getId(),
                    savedReservation.getUserId(),
                    savedReservation.getAssetId()
            ));
        } else {
            eventPublisher.publish(ReservationRejectedEvent.create(
                    savedReservation.getId(),
                    savedReservation.getUserId(),
                    savedReservation.getAssetId(),
                    savedReservation.getRejectionReason()
            ));
        }

        return toResult(savedReservation);
    }

    private ReservationResult toResult(Reservation reservation) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getAssetId(),
                reservation.getRentalPeriod().from(),
                reservation.getRentalPeriod().to(),
                reservation.getStatus(),
                reservation.getRejectionReason(),
                reservation.getCreatedAt()
        );
    }
}