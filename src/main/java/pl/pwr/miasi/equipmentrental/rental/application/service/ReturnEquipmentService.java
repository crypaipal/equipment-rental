package pl.pwr.miasi.equipmentrental.rental.application.service;

import pl.pwr.miasi.equipmentrental.rental.application.command.ReturnEquipmentCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.ReturnEquipmentUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.RentalRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;
import pl.pwr.miasi.equipmentrental.rental.domain.Rental;
import pl.pwr.miasi.equipmentrental.rental.domain.events.EquipmentReturnedEvent;
import pl.pwr.miasi.equipmentrental.rental.domain.events.EquipmentReturnedWithDamageEvent;
import pl.pwr.miasi.equipmentrental.rental.domain.events.RentalOverdueEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

import java.time.Instant;

public class ReturnEquipmentService implements ReturnEquipmentUseCase {

    private final RentalRepository rentalRepository;
    private final EventPublisher eventPublisher;

    public ReturnEquipmentService(
            RentalRepository rentalRepository,
            EventPublisher eventPublisher
    ) {
        this.rentalRepository = rentalRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RentalResult returnEquipment(ReturnEquipmentCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new NotFoundException("Rental not found"));

        if (command.damaged() && (command.damageReport() == null || command.damageReport().isBlank())) {
            throw new BusinessException("Damage report is required when returned asset is damaged");
        }

        Instant returnedAt = command.returnedAt() == null
                ? Instant.now()
                : command.returnedAt();

        rental.returnEquipment(returnedAt);

        Rental savedRental = rentalRepository.save(rental);

        if (command.damaged()) {
            eventPublisher.publish(EquipmentReturnedWithDamageEvent.create(
                    savedRental.getId(),
                    savedRental.getUserId(),
                    savedRental.getAssetId(),
                    command.damageReport()
            ));
        }

        if (savedRental.isOverdue()) {
            eventPublisher.publish(RentalOverdueEvent.create(
                    savedRental.getId(),
                    savedRental.getUserId(),
                    savedRental.getAssetId(),
                    savedRental.getExpectedReturnAt(),
                    savedRental.getReturnedAt()
            ));
        }

        eventPublisher.publish(EquipmentReturnedEvent.create(
                savedRental.getId(),
                savedRental.getReservationId(),
                savedRental.getUserId(),
                savedRental.getAssetId(),
                savedRental.getReturnedAt(),
                command.damaged()
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