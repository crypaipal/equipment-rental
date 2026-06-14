package pl.pwr.miasi.equipmentrental.rental.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.rental.application.command.ReturnEquipmentCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.RentalRepository;
import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;
import pl.pwr.miasi.equipmentrental.rental.domain.Rental;
import pl.pwr.miasi.equipmentrental.rental.domain.RentalStatus;
import pl.pwr.miasi.equipmentrental.rental.domain.events.EquipmentReturnedEvent;
import pl.pwr.miasi.equipmentrental.rental.domain.events.EquipmentReturnedWithDamageEvent;
import pl.pwr.miasi.equipmentrental.rental.domain.events.RentalOverdueEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReturnEquipmentServiceTest {

    private static final Instant CHECKOUT_AT = Instant.parse("2026-06-01T10:00:00Z");
    private static final Instant EXPECTED_RETURN_AT = Instant.parse("2026-06-01T12:00:00Z");

    @Test
    void returnsEquipmentOnTimeAndPublishesOnlyReturnedEvent() {
        Rental rental = activeRental();
        FakeRentalRepository rentalRepository = new FakeRentalRepository(rental);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ReturnEquipmentService service = new ReturnEquipmentService(rentalRepository, eventPublisher);
        Instant returnedAt = Instant.parse("2026-06-01T11:30:00Z");

        RentalResult result = service.returnEquipment(new ReturnEquipmentCommand(
                rental.getId(),
                false,
                null,
                returnedAt
        ));

        assertThat(result.status()).isEqualTo(RentalStatus.CLOSED);
        assertThat(result.returnedAt()).isEqualTo(returnedAt);
        assertThat(rentalRepository.savedRental).isSameAs(rental);

        assertThat(eventPublisher.events).singleElement().isInstanceOf(EquipmentReturnedEvent.class);
        EquipmentReturnedEvent event = (EquipmentReturnedEvent) eventPublisher.events.getFirst();
        assertThat(event.rentalId()).isEqualTo(rental.getId());
        assertThat(event.reservationId()).isEqualTo(rental.getReservationId());
        assertThat(event.userId()).isEqualTo(rental.getUserId());
        assertThat(event.assetId()).isEqualTo(rental.getAssetId());
        assertThat(event.returnedAt()).isEqualTo(returnedAt);
        assertThat(event.damaged()).isFalse();
    }

    @Test
    void returnsOverdueEquipmentAndPublishesOverdueEventBeforeReturnedEvent() {
        Rental rental = activeRental();
        FakeRentalRepository rentalRepository = new FakeRentalRepository(rental);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ReturnEquipmentService service = new ReturnEquipmentService(rentalRepository, eventPublisher);
        Instant returnedAt = Instant.parse("2026-06-01T13:00:00Z");

        RentalResult result = service.returnEquipment(new ReturnEquipmentCommand(
                rental.getId(),
                false,
                null,
                returnedAt
        ));

        assertThat(result.status()).isEqualTo(RentalStatus.CLOSED);
        assertThat(result.returnedAt()).isEqualTo(returnedAt);
        assertThat(eventPublisher.events).hasSize(2);

        assertThat(eventPublisher.events.get(0)).isInstanceOf(RentalOverdueEvent.class);
        RentalOverdueEvent overdueEvent = (RentalOverdueEvent) eventPublisher.events.get(0);
        assertThat(overdueEvent.rentalId()).isEqualTo(rental.getId());
        assertThat(overdueEvent.userId()).isEqualTo(rental.getUserId());
        assertThat(overdueEvent.assetId()).isEqualTo(rental.getAssetId());
        assertThat(overdueEvent.expectedReturnAt()).isEqualTo(EXPECTED_RETURN_AT);
        assertThat(overdueEvent.actualReturnAt()).isEqualTo(returnedAt);

        assertThat(eventPublisher.events.get(1)).isInstanceOf(EquipmentReturnedEvent.class);
        EquipmentReturnedEvent returnedEvent = (EquipmentReturnedEvent) eventPublisher.events.get(1);
        assertThat(returnedEvent.rentalId()).isEqualTo(rental.getId());
        assertThat(returnedEvent.damaged()).isFalse();
    }

    @Test
    void returnsDamagedEquipmentAndPublishesDamageEventBeforeReturnedEvent() {
        Rental rental = activeRental();
        FakeRentalRepository rentalRepository = new FakeRentalRepository(rental);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ReturnEquipmentService service = new ReturnEquipmentService(rentalRepository, eventPublisher);
        Instant returnedAt = Instant.parse("2026-06-01T11:30:00Z");

        RentalResult result = service.returnEquipment(new ReturnEquipmentCommand(
                rental.getId(),
                true,
                "Broken lens",
                returnedAt
        ));

        assertThat(result.status()).isEqualTo(RentalStatus.CLOSED);
        assertThat(result.returnedAt()).isEqualTo(returnedAt);
        assertThat(eventPublisher.events).hasSize(2);

        assertThat(eventPublisher.events.get(0)).isInstanceOf(EquipmentReturnedWithDamageEvent.class);
        EquipmentReturnedWithDamageEvent damageEvent = (EquipmentReturnedWithDamageEvent) eventPublisher.events.get(0);
        assertThat(damageEvent.rentalId()).isEqualTo(rental.getId());
        assertThat(damageEvent.userId()).isEqualTo(rental.getUserId());
        assertThat(damageEvent.assetId()).isEqualTo(rental.getAssetId());
        assertThat(damageEvent.damageReport()).isEqualTo("Broken lens");

        assertThat(eventPublisher.events.get(1)).isInstanceOf(EquipmentReturnedEvent.class);
        EquipmentReturnedEvent returnedEvent = (EquipmentReturnedEvent) eventPublisher.events.get(1);
        assertThat(returnedEvent.rentalId()).isEqualTo(rental.getId());
        assertThat(returnedEvent.damaged()).isTrue();
    }

    @Test
    void rejectsDamagedReturnWithoutDamageReport() {
        Rental rental = activeRental();
        FakeRentalRepository rentalRepository = new FakeRentalRepository(rental);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        ReturnEquipmentService service = new ReturnEquipmentService(rentalRepository, eventPublisher);

        assertThatThrownBy(() -> service.returnEquipment(new ReturnEquipmentCommand(
                rental.getId(),
                true,
                " ",
                Instant.parse("2026-06-01T11:30:00Z")
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Damage report is required when returned asset is damaged");

        assertThat(rental.getStatus()).isEqualTo(RentalStatus.ACTIVE);
        assertThat(rentalRepository.savedRental).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    private static Rental activeRental() {
        return new Rental(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                CHECKOUT_AT,
                EXPECTED_RETURN_AT,
                null,
                RentalStatus.ACTIVE
        );
    }

    private static class FakeRentalRepository implements RentalRepository {
        private final Rental rental;
        private Rental savedRental;

        private FakeRentalRepository(Rental rental) {
            this.rental = rental;
        }

        @Override
        public Rental save(Rental rental) {
            savedRental = rental;
            return rental;
        }

        @Override
        public Optional<Rental> findById(UUID id) {
            if (rental.getId().equals(id)) {
                return Optional.of(rental);
            }

            return Optional.empty();
        }

        @Override
        public List<Rental> findAll() {
            return List.of(rental);
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
