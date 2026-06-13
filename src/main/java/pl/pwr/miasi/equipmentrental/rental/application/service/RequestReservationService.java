package pl.pwr.miasi.equipmentrental.rental.application.service;

import pl.pwr.miasi.equipmentrental.rental.application.command.RequestReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.RequestReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.UserAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalEligibilityRule;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalPeriod;
import pl.pwr.miasi.equipmentrental.rental.domain.Reservation;
import pl.pwr.miasi.equipmentrental.rental.domain.events.ReservationRequestedEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

public class RequestReservationService implements RequestReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final UserAccessPort userAccessPort;
    private final InventoryAssetAccessPort inventoryAssetAccessPort;
    private final EventPublisher eventPublisher;

    public RequestReservationService(
            ReservationRepository reservationRepository,
            UserAccessPort userAccessPort,
            InventoryAssetAccessPort inventoryAssetAccessPort,
            EventPublisher eventPublisher
    ) {
        this.reservationRepository = reservationRepository;
        this.userAccessPort = userAccessPort;
        this.inventoryAssetAccessPort = inventoryAssetAccessPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ReservationResult request(RequestReservationCommand command) {
        RentalPeriod rentalPeriod = new RentalPeriod(command.periodFrom(), command.periodTo());

        RentalEligibilityRule rentalEligibilityRule = new RentalEligibilityRule(userAccessPort);

        if (!rentalEligibilityRule.isStudentEligible(command.userId())) {
            throw new BusinessException("User is not allowed to rent equipment");
        }

        if (!inventoryAssetAccessPort.isAssetAvailableForRental(command.assetId())) {
            throw new BusinessException("Asset is not available for rental");
        }

        if (reservationRepository.existsActiveReservationForAsset(command.assetId(), rentalPeriod)) {
            throw new BusinessException("Asset is already reserved in selected period");
        }

        Reservation reservation = Reservation.request(
                command.userId(),
                command.assetId(),
                rentalPeriod
        );

        Reservation savedReservation = reservationRepository.save(reservation);

        eventPublisher.publish(ReservationRequestedEvent.create(
                savedReservation.getId(),
                savedReservation.getUserId(),
                savedReservation.getAssetId(),
                savedReservation.getRentalPeriod().from(),
                savedReservation.getRentalPeriod().to()
        ));

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