package pl.pwr.miasi.equipmentrental.identity.infrastructure.events;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.identity.application.command.LockUserAccountCommand;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.LockUserAccountUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.result.UserResult;
import pl.pwr.miasi.equipmentrental.rental.domain.events.RentalOverdueEvent;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RentalOverdueEventHandlerTest {

    @Test
    void handlesRentalOverdueEventByLockingUserAccount() {
        RecordingLockUserAccountUseCase lockUserAccountUseCase = new RecordingLockUserAccountUseCase();
        RentalOverdueEventHandler handler = new RentalOverdueEventHandler(lockUserAccountUseCase);
        UUID userId = UUID.randomUUID();
        RentalOverdueEvent event = RentalOverdueEvent.create(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                Instant.now().minusSeconds(3600),
                Instant.now()
        );

        handler.handle(event);

        assertThat(lockUserAccountUseCase.command).isNotNull();
        assertThat(lockUserAccountUseCase.command.userId()).isEqualTo(userId);
        assertThat(lockUserAccountUseCase.command.reason())
                .isEqualTo("Rental returned after expected return date");
    }

    @Test
    void exposesRentalOverdueEventType() {
        RentalOverdueEventHandler handler = new RentalOverdueEventHandler(new RecordingLockUserAccountUseCase());

        assertThat(handler.eventType()).isEqualTo(RentalOverdueEvent.class);
    }

    private static class RecordingLockUserAccountUseCase implements LockUserAccountUseCase {
        private LockUserAccountCommand command;

        @Override
        public UserResult lockUserAccount(LockUserAccountCommand command) {
            this.command = command;
            return null;
        }
    }
}
