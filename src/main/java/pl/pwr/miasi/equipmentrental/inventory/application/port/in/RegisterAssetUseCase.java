package pl.pwr.miasi.equipmentrental.inventory.application.port.in;

import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterAssetCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;

public interface RegisterAssetUseCase {

    AssetResult register(RegisterAssetCommand command);
}