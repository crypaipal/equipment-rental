package pl.pwr.miasi.equipmentrental.shared.infrastructure;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.shared.application.DomainEventHandler;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

@Component
public class InMemoryEventPublisher implements EventPublisher {

    private final ObjectProvider<DomainEventHandler<? extends DomainEvent>> handlersProvider;

    public InMemoryEventPublisher(
            ObjectProvider<DomainEventHandler<? extends DomainEvent>> handlersProvider
    ) {
        this.handlersProvider = handlersProvider;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void publish(DomainEvent event) {
        System.out.println("DOMAIN EVENT: " + event.eventType() + " " + event);

        handlersProvider.stream()
                .filter(handler -> handler.eventType().equals(event.getClass()))
                .forEach(handler -> ((DomainEventHandler) handler).handle(event));
    }
}