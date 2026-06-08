package pl.pwr.miasi.equipmentrental.inventory.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterEquipmentModelCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.RegisterEquipmentModelUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.result.EquipmentModelResult;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.FindAllEquipmentModelsUseCase;
import java.util.List;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.AuthenticatedUserResolver;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.RoleGuard;

@RestController
@RequestMapping("/api/inventory/models")
public class EquipmentModelController {

    private final RegisterEquipmentModelUseCase registerEquipmentModelUseCase;
    private final FindAllEquipmentModelsUseCase findAllEquipmentModelsUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final RoleGuard roleGuard;

    public EquipmentModelController(
            RegisterEquipmentModelUseCase registerEquipmentModelUseCase,
            FindAllEquipmentModelsUseCase findAllEquipmentModelsUseCase,
            AuthenticatedUserResolver authenticatedUserResolver,
            RoleGuard roleGuard
    ) {
        this.registerEquipmentModelUseCase = registerEquipmentModelUseCase;
        this.findAllEquipmentModelsUseCase = findAllEquipmentModelsUseCase;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.roleGuard = roleGuard;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentModelResponse register(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody RegisterEquipmentModelRequest request
    ) {
        var user = authenticatedUserResolver.resolve(authorizationHeader);
        roleGuard.requireAnyRole(user, Role.SYSTEM_ADMIN);
        EquipmentModelResult result = registerEquipmentModelUseCase.register(
                new RegisterEquipmentModelCommand(
                        request.name(),
                        request.category(),
                        request.manufacturer()
                )
        );

        return new EquipmentModelResponse(
                result.id(),
                result.name(),
                result.category(),
                result.manufacturer()
        );
    }

    @GetMapping
    public List<EquipmentModelResponse> findAll() {
        return findAllEquipmentModelsUseCase.findAll()
                .stream()
                .map(result -> new EquipmentModelResponse(
                        result.id(),
                        result.name(),
                        result.category(),
                        result.manufacturer()
                ))
                .toList();
    }
}