package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.rental.application.command.FindAvailableEquipmentQuery;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.FindAvailableEquipmentUseCase;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/rental/available-assets")
public class AvailableEquipmentController {

    private final FindAvailableEquipmentUseCase findAvailableEquipmentUseCase;

    public AvailableEquipmentController(FindAvailableEquipmentUseCase findAvailableEquipmentUseCase) {
        this.findAvailableEquipmentUseCase = findAvailableEquipmentUseCase;
    }

    @GetMapping
    public List<AvailableAssetResponse> findAvailableAssets(
            @RequestParam String category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodTo
    ) {
        return findAvailableEquipmentUseCase.findAvailable(
                        new FindAvailableEquipmentQuery(
                                category,
                                periodFrom,
                                periodTo
                        )
                )
                .stream()
                .map(asset -> new AvailableAssetResponse(
                        asset.assetId(),
                        asset.equipmentModelId(),
                        asset.inventoryTag(),
                        asset.modelName(),
                        asset.category(),
                        asset.manufacturer()
                ))
                .toList();
    }
}