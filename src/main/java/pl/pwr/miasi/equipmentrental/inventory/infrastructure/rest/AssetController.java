package pl.pwr.miasi.equipmentrental.inventory.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterAssetCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.RegisterAssetUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;

@RestController
@RequestMapping("/api/inventory/assets")
public class AssetController {

    private final RegisterAssetUseCase registerAssetUseCase;

    public AssetController(RegisterAssetUseCase registerAssetUseCase) {
        this.registerAssetUseCase = registerAssetUseCase;
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

        return new AssetResponse(
                result.id(),
                result.equipmentModelId(),
                result.inventoryTag(),
                result.condition(),
                result.damageReport()
        );
    }
}