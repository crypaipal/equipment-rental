package pl.pwr.miasi.equipmentrental.rental.application.port.in;

import pl.pwr.miasi.equipmentrental.rental.application.command.FindAvailableEquipmentQuery;
import pl.pwr.miasi.equipmentrental.rental.application.result.AvailableAssetResult;

import java.util.List;

public interface FindAvailableEquipmentUseCase {

    List<AvailableAssetResult> findAvailable(FindAvailableEquipmentQuery query);
}