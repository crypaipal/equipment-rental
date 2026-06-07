package pl.pwr.miasi.equipmentrental.inventory.infrastructure.events;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.inventory.application.command.ReportAssetDamageCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.ReportAssetDamageUseCase;
import pl.pwr.miasi.equipmentrental.rental.domain.events.EquipmentReturnedWithDamageEvent;
import pl.pwr.miasi.equipmentrental.shared.application.DomainEventHandler;

@Component
public class EquipmentReturnedWithDamageEventHandler implements DomainEventHandler<EquipmentReturnedWithDamageEvent> {

    private final ReportAssetDamageUseCase reportAssetDamageUseCase;

    public EquipmentReturnedWithDamageEventHandler(ReportAssetDamageUseCase reportAssetDamageUseCase) {
        this.reportAssetDamageUseCase = reportAssetDamageUseCase;
    }

    @Override
    public Class<EquipmentReturnedWithDamageEvent> eventType() {
        return EquipmentReturnedWithDamageEvent.class;
    }

    @Override
    public void handle(EquipmentReturnedWithDamageEvent event) {
        reportAssetDamageUseCase.reportAssetDamage(new ReportAssetDamageCommand(
                event.assetId(),
                event.damageReport()
        ));
    }
}