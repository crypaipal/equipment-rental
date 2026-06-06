package pl.pwr.miasi.equipmentrental.rental.infrastructure.integration;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;
import pl.pwr.miasi.equipmentrental.identity.domain.User;
import pl.pwr.miasi.equipmentrental.identity.domain.events.UserBlockedEvent;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.UserAccessPort;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

import java.time.Instant;
import java.util.UUID;

@Component
public class IdentityUserAccessAdapter implements UserAccessPort {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public IdentityUserAccessAdapter(
            UserRepository userRepository,
            EventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean canUserRent(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> !user.isLocked())
                .orElse(false);
    }

    @Override
    public void blockUserDueToOverdue(UUID userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Instant lockedUntil = Instant.now().plusSeconds(30L * 24 * 60 * 60);

        user.lockAccount(lockedUntil, reason);
        User savedUser = userRepository.save(user);

        eventPublisher.publish(UserBlockedEvent.create(
                savedUser.getId(),
                savedUser.getLockedUntil(),
                savedUser.getLockReason()
        ));
    }
}