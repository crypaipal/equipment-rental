package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.rental.application.command.ReturnEquipmentCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.ReturnEquipmentUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;

import java.util.UUID;

@RestController
@RequestMapping("/api/rental/rentals")
public class RentalController {

    private final ReturnEquipmentUseCase returnEquipmentUseCase;

    public RentalController(ReturnEquipmentUseCase returnEquipmentUseCase) {
        this.returnEquipmentUseCase = returnEquipmentUseCase;
    }

    @PostMapping("/{rentalId}/return")
    public RentalResponse returnEquipment(
            @PathVariable UUID rentalId,
            @RequestBody(required = false) ReturnEquipmentRequest request
    ) {
        boolean damaged = request != null && Boolean.TRUE.equals(request.damaged());
        String damageReport = request == null ? null : request.damageReport();
        java.time.Instant returnedAt = request == null ? null : request.returnedAt();

        RentalResult result = returnEquipmentUseCase.returnEquipment(
                new ReturnEquipmentCommand(
                        rentalId,
                        damaged,
                        damageReport,
                        returnedAt
                )
        );

        return new RentalResponse(
                result.id(),
                result.reservationId(),
                result.userId(),
                result.assetId(),
                result.checkoutAt(),
                result.expectedReturnAt(),
                result.returnedAt(),
                result.status()
        );
    }
}