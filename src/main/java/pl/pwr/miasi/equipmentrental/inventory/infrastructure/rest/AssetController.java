package pl.pwr.miasi.equipmentrental.inventory.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.inventory.application.command.ChangeAssetConditionCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterAssetCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.ChangeAssetConditionUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.RegisterAssetUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/assets")
public class AssetController {

    private final RegisterAssetUseCase registerAssetUseCase;
    private final ChangeAssetConditionUseCase changeAssetConditionUseCase;

    public AssetController(
            RegisterAssetUseCase registerAssetUseCase,
            ChangeAssetConditionUseCase changeAssetConditionUseCase
    ) {
        this.registerAssetUseCase = registerAssetUseCase;
        this.changeAssetConditionUseCase = changeAssetConditionUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse register(@Valid @RequestBody RegisterAssetRequest request) {
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
            @PathVariable UUID assetId,
            @Valid @RequestBody ChangeAssetConditionRequest request
    ) {
        AssetResult result = changeAssetConditionUseCase.changeCondition(
                new ChangeAssetConditionCommand(
                        assetId,
                        request.condition(),
                        request.damageReport()
                )
        );

        return toResponse(result);
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