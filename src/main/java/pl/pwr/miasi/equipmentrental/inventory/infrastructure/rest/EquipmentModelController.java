package pl.pwr.miasi.equipmentrental.inventory.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterEquipmentModelCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.RegisterEquipmentModelUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.result.EquipmentModelResult;

@RestController
@RequestMapping("/api/inventory/models")
public class EquipmentModelController {

    private final RegisterEquipmentModelUseCase registerEquipmentModelUseCase;

    public EquipmentModelController(RegisterEquipmentModelUseCase registerEquipmentModelUseCase) {
        this.registerEquipmentModelUseCase = registerEquipmentModelUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentModelResponse register(@Valid @RequestBody RegisterEquipmentModelRequest request) {
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
}