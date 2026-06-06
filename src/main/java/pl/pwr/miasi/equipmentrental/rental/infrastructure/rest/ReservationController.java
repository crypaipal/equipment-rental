package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.rental.application.command.CheckoutEquipmentCommand;
import pl.pwr.miasi.equipmentrental.rental.application.command.RequestReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.command.ReviewReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.CheckoutEquipmentUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.RequestReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.ReviewReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;
import pl.pwr.miasi.equipmentrental.rental.application.command.CancelReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.CancelReservationUseCase;

import java.util.UUID;

@RestController
@RequestMapping("/api/rental/reservations")
public class ReservationController {

    private final RequestReservationUseCase requestReservationUseCase;
    private final ReviewReservationUseCase reviewReservationUseCase;
    private final CheckoutEquipmentUseCase checkoutEquipmentUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;

    public ReservationController(
            RequestReservationUseCase requestReservationUseCase,
            ReviewReservationUseCase reviewReservationUseCase,
            CheckoutEquipmentUseCase checkoutEquipmentUseCase,
            CancelReservationUseCase cancelReservationUseCase
    ) {
        this.requestReservationUseCase = requestReservationUseCase;
        this.reviewReservationUseCase = reviewReservationUseCase;
        this.checkoutEquipmentUseCase = checkoutEquipmentUseCase;
        this.cancelReservationUseCase = cancelReservationUseCase;
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

        return toReservationResponse(result);
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

        return toReservationResponse(result);
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

        return toReservationResponse(result);
    }

    @PostMapping("/{reservationId}/cancel")
    public ReservationResponse cancel(@PathVariable UUID reservationId) {
        ReservationResult result = cancelReservationUseCase.cancel(
                new CancelReservationCommand(reservationId)
        );

        return toReservationResponse(result);
    }

    @PostMapping("/{reservationId}/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse checkout(@PathVariable UUID reservationId) {
        RentalResult result = checkoutEquipmentUseCase.checkout(
                new CheckoutEquipmentCommand(reservationId)
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

    private ReservationResponse toReservationResponse(ReservationResult result) {
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