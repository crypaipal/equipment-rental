package pl.pwr.miasi.equipmentrental.rental.application.service;

import pl.pwr.miasi.equipmentrental.rental.application.command.CheckoutEquipmentCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.CheckoutEquipmentUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.RentalRepository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;
import pl.pwr.miasi.equipmentrental.rental.domain.Rental;
import pl.pwr.miasi.equipmentrental.rental.domain.Reservation;
import pl.pwr.miasi.equipmentrental.rental.domain.events.EquipmentCheckedOutEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

public class CheckoutEquipmentService implements CheckoutEquipmentUseCase {

    private final ReservationRepository reservationRepository;
    private final RentalRepository rentalRepository;
    private final InventoryAssetAccessPort inventoryAssetAccessPort;
    private final EventPublisher eventPublisher;

    public CheckoutEquipmentService(
            ReservationRepository reservationRepository,
            RentalRepository rentalRepository,
            InventoryAssetAccessPort inventoryAssetAccessPort,
            EventPublisher eventPublisher
    ) {
        this.reservationRepository = reservationRepository;
        this.rentalRepository = rentalRepository;
        this.inventoryAssetAccessPort = inventoryAssetAccessPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RentalResult checkout(CheckoutEquipmentCommand command) {
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        if (!inventoryAssetAccessPort.isAssetAvailableForRental(reservation.getAssetId())) {
            throw new BusinessException("Asset is not available for checkout");
        }

        reservation.fulfill();
        reservationRepository.save(reservation);

        Rental rental = Rental.checkout(reservation);
        Rental savedRental = rentalRepository.save(rental);

        eventPublisher.publish(EquipmentCheckedOutEvent.create(
                savedRental.getId(),
                savedRental.getReservationId(),
                savedRental.getUserId(),
                savedRental.getAssetId(),
                savedRental.getExpectedReturnAt()
        ));

        return new RentalResult(
                savedRental.getId(),
                savedRental.getReservationId(),
                savedRental.getUserId(),
                savedRental.getAssetId(),
                savedRental.getCheckoutAt(),
                savedRental.getExpectedReturnAt(),
                savedRental.getReturnedAt(),
                savedRental.getStatus()
        );
    }
}