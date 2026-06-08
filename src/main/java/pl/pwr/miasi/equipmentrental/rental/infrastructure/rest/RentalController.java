package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.rental.application.command.ReturnEquipmentCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.ReturnEquipmentUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.FindAllRentalsUseCase;
import java.util.List;
import java.time.Instant;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.AuthenticatedUserResolver;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.RoleGuard;

import java.util.UUID;

@RestController
@RequestMapping("/api/rental/rentals")
public class RentalController {

    private final ReturnEquipmentUseCase returnEquipmentUseCase;
    private final FindAllRentalsUseCase findAllRentalsUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final RoleGuard roleGuard;

    public RentalController(
            ReturnEquipmentUseCase returnEquipmentUseCase,
            FindAllRentalsUseCase findAllRentalsUseCase,
            AuthenticatedUserResolver authenticatedUserResolver,
            RoleGuard roleGuard
    ) {
        this.returnEquipmentUseCase = returnEquipmentUseCase;
        this.findAllRentalsUseCase = findAllRentalsUseCase;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.roleGuard = roleGuard;
    }

    @GetMapping
    public List<RentalResponse> findAll(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        var user = authenticatedUserResolver.resolve(authorizationHeader);
        roleGuard.requireAnyRole(user, Role.LAB_ASSISTANT, Role.SYSTEM_ADMIN);

        return findAllRentalsUseCase.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RentalResponse toResponse(RentalResult result) {
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

    @PostMapping("/{rentalId}/return")
    public RentalResponse returnEquipment(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID rentalId,
            @RequestBody(required = false) ReturnEquipmentRequest request
    ) {
        var user = authenticatedUserResolver.resolve(authorizationHeader);
        roleGuard.requireAnyRole(user, Role.LAB_ASSISTANT, Role.SYSTEM_ADMIN);

        boolean damaged = request != null && Boolean.TRUE.equals(request.damaged());
        String damageReport = request == null ? null : request.damageReport();
        Instant returnedAt = request == null ? null : request.returnedAt();

        RentalResult result = returnEquipmentUseCase.returnEquipment(
                new ReturnEquipmentCommand(
                        rentalId,
                        damaged,
                        damageReport,
                        returnedAt
                )
        );

        return toResponse(result);
    }
}