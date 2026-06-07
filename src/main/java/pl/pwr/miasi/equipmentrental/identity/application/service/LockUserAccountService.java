package pl.pwr.miasi.equipmentrental.identity.application.service;

import pl.pwr.miasi.equipmentrental.identity.application.command.LockUserAccountCommand;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.LockUserAccountUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;
import pl.pwr.miasi.equipmentrental.identity.application.result.UserResult;
import pl.pwr.miasi.equipmentrental.identity.domain.User;
import pl.pwr.miasi.equipmentrental.identity.domain.events.UserBlockedEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

import java.time.Instant;

public class LockUserAccountService implements LockUserAccountUseCase {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public LockUserAccountService(
            UserRepository userRepository,
            EventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserResult lockUserAccount(LockUserAccountCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isLocked()) {
            return toResult(user);
        }

        Instant lockedUntil = Instant.now().plusSeconds(30L * 24 * 60 * 60);
        String reason = command.reason() == null || command.reason().isBlank()
                ? "Rental returned after expected return date"
                : command.reason();

        user.lockAccount(lockedUntil, reason);

        User savedUser = userRepository.save(user);

        eventPublisher.publish(UserBlockedEvent.create(
                savedUser.getId(),
                savedUser.getLockedUntil(),
                savedUser.getLockReason()
        ));

        return toResult(savedUser);
    }

    private UserResult toResult(User user) {
        return new UserResult(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail().value(),
                user.getRole(),
                user.getLockedUntil(),
                user.getLockReason()
        );
    }
}