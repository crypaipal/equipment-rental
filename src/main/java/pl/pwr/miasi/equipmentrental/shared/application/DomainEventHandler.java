package pl.pwr.miasi.equipmentrental.shared.application;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

public interface DomainEventHandler<T extends DomainEvent> {

    Class<T> eventType();

    void handle(T event);
}