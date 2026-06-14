package pl.pwr.miasi.equipmentrental.inventory.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterEquipmentModelCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.out.EquipmentModelRepository;
import pl.pwr.miasi.equipmentrental.inventory.application.result.EquipmentModelResult;
import pl.pwr.miasi.equipmentrental.inventory.domain.EquipmentModel;
import pl.pwr.miasi.equipmentrental.inventory.domain.events.ModelRegisteredEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterEquipmentModelServiceTest {

    @Test
    void registersEquipmentModelAndPublishesModelRegisteredEvent() {
        FakeEquipmentModelRepository repository = new FakeEquipmentModelRepository();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RegisterEquipmentModelService service = new RegisterEquipmentModelService(repository, eventPublisher);

        EquipmentModelResult result = service.register(new RegisterEquipmentModelCommand(
                "A7 III",
                "Camera",
                "Sony"
        ));

        assertThat(repository.savedModel).isNotNull();
        assertThat(result.id()).isEqualTo(repository.savedModel.getId());
        assertThat(result.name()).isEqualTo("A7 III");
        assertThat(result.category()).isEqualTo("Camera");
        assertThat(result.manufacturer()).isEqualTo("Sony");

        assertThat(eventPublisher.events).singleElement().isInstanceOf(ModelRegisteredEvent.class);
        ModelRegisteredEvent event = (ModelRegisteredEvent) eventPublisher.events.get(0);
        assertThat(event.equipmentModelId()).isEqualTo(result.id());
        assertThat(event.name()).isEqualTo("A7 III");
        assertThat(event.category()).isEqualTo("Camera");
        assertThat(event.manufacturer()).isEqualTo("Sony");
    }

    @Test
    void rejectsDuplicateModelNameAndManufacturerWithoutSavingOrPublishingEvent() {
        FakeEquipmentModelRepository repository = new FakeEquipmentModelRepository();
        repository.duplicateExists = true;
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RegisterEquipmentModelService service = new RegisterEquipmentModelService(repository, eventPublisher);

        assertThatThrownBy(() -> service.register(new RegisterEquipmentModelCommand(
                "A7 III",
                "Camera",
                "Sony"
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Equipment model with this name and manufacturer already exists");

        assertThat(repository.savedModel).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    private static class FakeEquipmentModelRepository implements EquipmentModelRepository {
        private boolean duplicateExists;
        private EquipmentModel savedModel;

        @Override
        public EquipmentModel save(EquipmentModel equipmentModel) {
            savedModel = equipmentModel;
            return equipmentModel;
        }

        @Override
        public boolean existsByNameAndManufacturer(String name, String manufacturer) {
            return duplicateExists;
        }

        @Override
        public boolean existsById(UUID id) {
            return false;
        }

        @Override
        public List<EquipmentModel> findAll() {
            return List.of();
        }
    }

    private static class RecordingEventPublisher implements EventPublisher {
        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }
    }
}
