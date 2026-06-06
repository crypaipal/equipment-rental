package pl.pwr.miasi.equipmentrental.identity.application.service;

import pl.pwr.miasi.equipmentrental.identity.application.command.RegisterUserCommand;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.RegisterUserUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.PasswordHasher;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;
import pl.pwr.miasi.equipmentrental.identity.application.result.UserResult;
import pl.pwr.miasi.equipmentrental.identity.domain.Email;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.identity.domain.User;
import pl.pwr.miasi.equipmentrental.identity.domain.events.UserRegisteredEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final EventPublisher eventPublisher;

    public RegisterUserService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            EventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserResult register(RegisterUserCommand command) {
        Email email = new Email(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("User with this email already exists");
        }

        if (command.password() == null || command.password().length() < 6) {
            throw new BusinessException("Password must have at least 6 characters");
        }

        Role role = command.role() == null ? Role.BORROWER : command.role();
        String passwordHash = passwordHasher.hash(command.password());

        User user = User.register(
                command.firstName(),
                command.lastName(),
                email,
                passwordHash,
                role
        );

        User savedUser = userRepository.save(user);

        eventPublisher.publish(UserRegisteredEvent.create(
                savedUser.getId(),
                savedUser.getEmail().value(),
                savedUser.getRole()
        ));

        return new UserResult(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail().value(),
                savedUser.getRole(),
                savedUser.getLockedUntil(),
                savedUser.getLockReason()
        );
    }
}