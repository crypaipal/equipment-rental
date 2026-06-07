package pl.pwr.miasi.equipmentrental.inventory.application.command;

import java.util.UUID;

public record ReportAssetDamageCommand(
        UUID assetId,
        String damageReport
) {
}