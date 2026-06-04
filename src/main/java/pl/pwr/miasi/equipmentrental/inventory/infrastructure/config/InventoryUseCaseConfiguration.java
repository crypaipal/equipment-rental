package pl.pwr.miasi.equipmentrental.inventory.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.RegisterEquipmentModelUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.EquipmentModelRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.service.RegisterEquipmentModelService;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;

@Configuration
public class InventoryUseCaseConfiguration {

    @Bean
    public RegisterEquipmentModelUseCase registerEquipmentModelUseCase(
            EquipmentModelRepository equipmentModelRepository,
            EventPublisher eventPublisher
    ) {
        return new RegisterEquipmentModelService(equipmentModelRepository, eventPublisher);
    }
}