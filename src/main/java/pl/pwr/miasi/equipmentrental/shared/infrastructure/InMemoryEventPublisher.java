package pl.pwr.miasi.equipmentrental.shared.infrastructure;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

@Component
public class InMemoryEventPublisher implements EventPublisher {

    @Override
    public void publish(DomainEvent event) {
        System.out.println("DOMAIN EVENT: " + event.eventType() + " " + event);
    }
}