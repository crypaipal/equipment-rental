package pl.pwr.miasi.equipmentrental.inventory.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.ChangeAssetConditionUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.RegisterAssetUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.RegisterEquipmentModelUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.AssetRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.EquipmentModelRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.service.ChangeAssetConditionService;
import pl.pwr.miasi.equipmentrental.inventory.application.service.RegisterAssetService;
import pl.pwr.miasi.equipmentrental.inventory.application.service.RegisterEquipmentModelService;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.ReportAssetDamageUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.service.ReportAssetDamageService;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.FindAllEquipmentModelsUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.service.FindAllEquipmentModelsService;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.FindAllAssetsUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.service.FindAllAssetsService;

@Configuration
public class InventoryUseCaseConfiguration {

    @Bean
    public RegisterEquipmentModelUseCase registerEquipmentModelUseCase(
            EquipmentModelRepository equipmentModelRepository,
            EventPublisher eventPublisher
    ) {
        return new RegisterEquipmentModelService(equipmentModelRepository, eventPublisher);
    }

    @Bean
    public RegisterAssetUseCase registerAssetUseCase(
            AssetRepository assetRepository,
            EquipmentModelRepository equipmentModelRepository,
            EventPublisher eventPublisher
    ) {
        return new RegisterAssetService(
                assetRepository,
                equipmentModelRepository,
                eventPublisher
        );
    }

    @Bean
    public ChangeAssetConditionUseCase changeAssetConditionUseCase(
            AssetRepository assetRepository,
            EventPublisher eventPublisher
    ) {
        return new ChangeAssetConditionService(
                assetRepository,
                eventPublisher
        );
    }

    @Bean
    ReportAssetDamageUseCase reportAssetDamageUseCase(
            AssetRepository assetRepository,
            EventPublisher eventPublisher
    ) {
        return new ReportAssetDamageService(assetRepository, eventPublisher);
    }

    @Bean
    public FindAllEquipmentModelsUseCase findAllEquipmentModelsUseCase(
            EquipmentModelRepository equipmentModelRepository
    ) {
        return new FindAllEquipmentModelsService(equipmentModelRepository);
    }

    @Bean
    public FindAllAssetsUseCase findAllAssetsUseCase(
            AssetRepository assetRepository
    ) {
        return new FindAllAssetsService(assetRepository);
    }
}