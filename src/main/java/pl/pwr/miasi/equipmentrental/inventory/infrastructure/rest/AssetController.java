package pl.pwr.miasi.equipmentrental.inventory.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.inventory.application.command.ChangeAssetConditionCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterAssetCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.ChangeAssetConditionUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.RegisterAssetUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.FindAllAssetsUseCase;
import java.util.List;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.AuthenticatedUserResolver;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.RoleGuard;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/assets")
public class AssetController {

    private final RegisterAssetUseCase registerAssetUseCase;
    private final ChangeAssetConditionUseCase changeAssetConditionUseCase;
    private final FindAllAssetsUseCase findAllAssetsUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final RoleGuard roleGuard;

    public AssetController(
            RegisterAssetUseCase registerAssetUseCase,
            ChangeAssetConditionUseCase changeAssetConditionUseCase,
            FindAllAssetsUseCase findAllAssetsUseCase,
            AuthenticatedUserResolver authenticatedUserResolver,
            RoleGuard roleGuard
    ) {
        this.registerAssetUseCase = registerAssetUseCase;
        this.changeAssetConditionUseCase = changeAssetConditionUseCase;
        this.findAllAssetsUseCase = findAllAssetsUseCase;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.roleGuard = roleGuard;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse register(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody RegisterAssetRequest request
    ) {
        var user = authenticatedUserResolver.resolve(authorizationHeader);
        roleGuard.requireAnyRole(user, Role.SYSTEM_ADMIN);
        AssetResult result = registerAssetUseCase.register(
                new RegisterAssetCommand(
                        request.equipmentModelId(),
                        request.inventoryTag()
                )
        );

        return toResponse(result);
    }

    @PatchMapping("/{assetId}/condition")
    public AssetResponse changeCondition(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable UUID assetId,
            @Valid @RequestBody ChangeAssetConditionRequest request
    ) {
        var user = authenticatedUserResolver.resolve(authorizationHeader);
        roleGuard.requireAnyRole(user, Role.LAB_ASSISTANT, Role.SYSTEM_ADMIN);
        AssetResult result = changeAssetConditionUseCase.changeCondition(
                new ChangeAssetConditionCommand(
                        assetId,
                        request.condition(),
                        request.damageReport()
                )
        );

        return toResponse(result);
    }

    @GetMapping
    public List<AssetResponse> findAll() {
        return findAllAssetsUseCase.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AssetResponse toResponse(AssetResult result) {
        return new AssetResponse(
                result.id(),
                result.equipmentModelId(),
                result.inventoryTag(),
                result.condition(),
                result.damageReport()
        );
    }
}