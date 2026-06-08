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
import pl.pwr.miasi.equipmentrental.rental.application.port.in.FindAllReservationsUseCase;
import java.util.List;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.AuthenticatedUserResolver;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.RoleGuard;
import pl.pwr.miasi.equipmentrental.shared.exception.ForbiddenException;

import java.util.UUID;

@RestController
@RequestMapping("/api/rental/reservations")
public class ReservationController {

    private final RequestReservationUseCase requestReservationUseCase;
    private final ReviewReservationUseCase reviewReservationUseCase;
    private final CheckoutEquipmentUseCase checkoutEquipmentUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;
    private final FindAllReservationsUseCase findAllReservationsUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final RoleGuard roleGuard;

    public ReservationController(
            RequestReservationUseCase requestReservationUseCase,
            ReviewReservationUseCase reviewReservationUseCase,
            CheckoutEquipmentUseCase checkoutEquipmentUseCase,
            CancelReservationUseCase cancelReservationUseCase,
            FindAllReservationsUseCase findAllReservationsUseCase,
            AuthenticatedUserResolver authenticatedUserResolver,
            RoleGuard roleGuard
    ) {
        this.requestReservationUseCase = requestReservationUseCase;
        this.reviewReservationUseCase = reviewReservationUseCase;
        this.checkoutEquipmentUseCase = checkoutEquipmentUseCase;
        this.cancelReservationUseCase = cancelReservationUseCase;
        this.findAllReservationsUseCase = findAllReservationsUseCase;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.roleGuard = roleGuard;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse request(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody RequestReservationRequest request
    ) {
        var user = authenticatedUserResolver.resolve(authorizationHeader);
        roleGuard.requireAnyRole(user, Role.BORROWER);

        if (!user.userId().equals(request.userId())) {
            throw new ForbiddenException("You can create reservations only for your own account.");
        }
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
    public ReservationResponse approve(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID reservationId
    ) {
        var user = authenticatedUserResolver.resolve(authorizationHeader);
        roleGuard.requireAnyRole(user, Role.LAB_ASSISTANT, Role.SYSTEM_ADMIN);
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
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID reservationId,
            @RequestBody(required = false) RejectReservationRequest request
    ) {
        var user = authenticatedUserResolver.resolve(authorizationHeader);
        roleGuard.requireAnyRole(user, Role.LAB_ASSISTANT, Role.SYSTEM_ADMIN);
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
    public ReservationResponse cancel(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID reservationId
    ) {
        var user = authenticatedUserResolver.resolve(authorizationHeader);
        roleGuard.requireAnyRole(user, Role.BORROWER, Role.LAB_ASSISTANT, Role.SYSTEM_ADMIN);
        ReservationResult result = cancelReservationUseCase.cancel(
                new CancelReservationCommand(reservationId)
        );

        return toReservationResponse(result);
    }

    @PostMapping("/{reservationId}/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse checkout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID reservationId
    ) {
        var user = authenticatedUserResolver.resolve(authorizationHeader);
        roleGuard.requireAnyRole(user, Role.LAB_ASSISTANT, Role.SYSTEM_ADMIN);
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

    @GetMapping
    public List<ReservationResponse> findAll(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        var user = authenticatedUserResolver.resolve(authorizationHeader);
        roleGuard.requireAnyRole(user, Role.BORROWER, Role.LAB_ASSISTANT, Role.SYSTEM_ADMIN);
        return findAllReservationsUseCase.findAll()
                .stream()
                .map(this::toReservationResponse)
                .toList();
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