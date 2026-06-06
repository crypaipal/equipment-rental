package pl.pwr.miasi.equipmentrental.rental.application.service;

import pl.pwr.miasi.equipmentrental.rental.application.command.FindAvailableEquipmentQuery;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.FindAvailableEquipmentUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.AvailableAssetResult;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalPeriod;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FindAvailableEquipmentService implements FindAvailableEquipmentUseCase {

    private final InventoryAssetAccessPort inventoryAssetAccessPort;
    private final ReservationRepository reservationRepository;

    public FindAvailableEquipmentService(
            InventoryAssetAccessPort inventoryAssetAccessPort,
            ReservationRepository reservationRepository
    ) {
        this.inventoryAssetAccessPort = inventoryAssetAccessPort;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<AvailableAssetResult> findAvailable(FindAvailableEquipmentQuery query) {
        if (query.category() == null || query.category().isBlank()) {
            throw new BusinessException("Category cannot be empty");
        }

        RentalPeriod rentalPeriod = new RentalPeriod(query.periodFrom(), query.periodTo());

        Set<UUID> reservedAssetIds = new HashSet<>(
                reservationRepository.findReservedAssetIds(rentalPeriod)
        );

        return inventoryAssetAccessPort.findAvailableAssetsByCategory(query.category())
                .stream()
                .filter(asset -> !reservedAssetIds.contains(asset.assetId()))
                .map(asset -> new AvailableAssetResult(
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