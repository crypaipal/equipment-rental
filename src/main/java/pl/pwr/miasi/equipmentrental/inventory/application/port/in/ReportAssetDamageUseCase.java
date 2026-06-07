package pl.pwr.miasi.equipmentrental.inventory.application.port.in;

import pl.pwr.miasi.equipmentrental.inventory.application.command.ReportAssetDamageCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;

public interface ReportAssetDamageUseCase {

    AssetResult reportAssetDamage(ReportAssetDamageCommand command);
}