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

        EquipmentModelResult result = service.register(
                new RegisterEquipmentModelCommand("A7 III", "Camera", "Sony")
        );

        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("A7 III");
        assertThat(result.category()).isEqualTo("Camera");
        assertThat(result.manufacturer()).isEqualTo("Sony");
        assertThat(repository.savedModel).isNotNull();
        assertThat(eventPublisher.publishedEvents)
                .singleElement()
                .isInstanceOf(ModelRegisteredEvent.class);
    }

    @Test
    void rejectsEquipmentModelWithDuplicateNameAndManufacturer() {
        FakeEquipmentModelRepository repository = new FakeEquipmentModelRepository();
        repository.duplicateNameAndManufacturer = true;
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RegisterEquipmentModelService service = new RegisterEquipmentModelService(repository, eventPublisher);

        assertThatThrownBy(() -> service.register(
                new RegisterEquipmentModelCommand("A7 III", "Camera", "Sony")
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Equipment model with this name and manufacturer already exists");

        assertThat(repository.savedModel).isNull();
        assertThat(eventPublisher.publishedEvents).isEmpty();
    }

    private static final class FakeEquipmentModelRepository implements EquipmentModelRepository {
        private boolean duplicateNameAndManufacturer;
        private EquipmentModel savedModel;

        @Override
        public EquipmentModel save(EquipmentModel equipmentModel) {
            savedModel = equipmentModel;
            return equipmentModel;
        }

        @Override
        public boolean existsByNameAndManufacturer(String name, String manufacturer) {
            return duplicateNameAndManufacturer;
        }

        @Override
        public boolean existsById(UUID id) {
            return savedModel != null && savedModel.getId().equals(id);
        }
    }

    private static final class RecordingEventPublisher implements EventPublisher {
        private final List<DomainEvent> publishedEvents = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            publishedEvents.add(event);
        }
    }
}
