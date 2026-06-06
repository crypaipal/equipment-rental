package pl.pwr.miasi.equipmentrental.inventory.application.port.in;

import pl.pwr.miasi.equipmentrental.inventory.application.command.ChangeAssetConditionCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;

public interface ChangeAssetConditionUseCase {

    AssetResult changeCondition(ChangeAssetConditionCommand command);
}