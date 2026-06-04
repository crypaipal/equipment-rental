package pl.pwr.miasi.equipmentrental.shared.application;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}
