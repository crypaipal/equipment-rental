package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.rental.application.command.RequestReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.command.ReviewReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.RequestReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.ReviewReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;

import java.util.UUID;

@RestController
@RequestMapping("/api/rental/reservations")
public class ReservationController {

    private final RequestReservationUseCase requestReservationUseCase;
    private final ReviewReservationUseCase reviewReservationUseCase;

    public ReservationController(
            RequestReservationUseCase requestReservationUseCase,
            ReviewReservationUseCase reviewReservationUseCase
    ) {
        this.requestReservationUseCase = requestReservationUseCase;
        this.reviewReservationUseCase = reviewReservationUseCase;
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

        return toResponse(result);
    }

    @PostMapping("/{reservationId}/approve")
    public ReservationResponse approve(@PathVariable UUID reservationId) {
        ReservationResult result = reviewReservationUseCase.review(
                new ReviewReservationCommand(
                        reservationId,
                        true,
                        null
                )
        );

        return toResponse(result);
    }

    @PostMapping("/{reservationId}/reject")
    public ReservationResponse reject(
            @PathVariable UUID reservationId,
            @RequestBody(required = false) RejectReservationRequest request
    ) {
        String rejectionReason = request == null ? null : request.rejectionReason();

        ReservationResult result = reviewReservationUseCase.review(
                new ReviewReservationCommand(
                        reservationId,
                        false,
                        rejectionReason
                )
        );

        return toResponse(result);
    }

    private ReservationResponse toResponse(ReservationResult result) {
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