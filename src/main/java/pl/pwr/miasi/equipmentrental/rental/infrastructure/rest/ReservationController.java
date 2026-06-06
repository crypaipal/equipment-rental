package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.rental.application.command.RequestReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.RequestReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;

@RestController
@RequestMapping("/api/rental/reservations")
public class ReservationController {

    private final RequestReservationUseCase requestReservationUseCase;

    public ReservationController(RequestReservationUseCase requestReservationUseCase) {
        this.requestReservationUseCase = requestReservationUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse request(@Valid @RequestBody RequestReservationRequest request) {
        ReservationResult result = requestReservationUseCase.request(
                new RequestReservationCommand(
                        request.userId(),
                        request.assetId(),
                        request.periodFrom(),
                        request.periodTo()
                )
        );

        return new ReservationResponse(
                result.id(),
                result.userId(),
                result.assetId(),
                result.periodFrom(),
                result.periodTo(),
                result.status(),
                result.rejectionReason(),
                result.createdAt()
        );
    }
}