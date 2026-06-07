package pl.pwr.miasi.equipmentrental.identity.infrastructure.events;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.identity.application.command.LockUserAccountCommand;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.LockUserAccountUseCase;
import pl.pwr.miasi.equipmentrental.rental.domain.events.RentalOverdueEvent;
import pl.pwr.miasi.equipmentrental.shared.application.DomainEventHandler;

@Component
public class RentalOverdueEventHandler implements DomainEventHandler<RentalOverdueEvent> {

    private final LockUserAccountUseCase lockUserAccountUseCase;

    public RentalOverdueEventHandler(LockUserAccountUseCase lockUserAccountUseCase) {
        this.lockUserAccountUseCase = lockUserAccountUseCase;
    }

    @Override
    public Class<RentalOverdueEvent> eventType() {
        return RentalOverdueEvent.class;
    }

    @Override
    public void handle(RentalOverdueEvent event) {
        lockUserAccountUseCase.lockUserAccount(new LockUserAccountCommand(
                event.userId(),
                "Rental returned after expected return date"
        ));
    }
}