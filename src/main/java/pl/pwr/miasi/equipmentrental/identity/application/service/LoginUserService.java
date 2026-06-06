package pl.pwr.miasi.equipmentrental.identity.application.service;

import pl.pwr.miasi.equipmentrental.identity.application.command.LoginUserCommand;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.LoginUserUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.AuthSessionRepository;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.PasswordHasher;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;
import pl.pwr.miasi.equipmentrental.identity.application.result.LoginResult;
import pl.pwr.miasi.equipmentrental.identity.domain.AuthSession;
import pl.pwr.miasi.equipmentrental.identity.domain.Email;
import pl.pwr.miasi.equipmentrental.identity.domain.User;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;

public class LoginUserService implements LoginUserUseCase {

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordHasher passwordHasher;

    public LoginUserService(
            UserRepository userRepository,
            AuthSessionRepository authSessionRepository,
            PasswordHasher passwordHasher
    ) {
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public LoginResult login(LoginUserCommand command) {
        Email email = new Email(command.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        if (user.isLocked()) {
            throw new BusinessException("User account is locked until " + user.getLockedUntil());
        }

        String providedPasswordHash = passwordHasher.hash(command.password());

        if (!user.getPasswordHash().equals(providedPasswordHash)) {
            user.recordFailedLogin(Instant.now().plusSeconds(15 * 60));
            userRepository.save(user);

            throw new BusinessException("Invalid email or password");
        }

        user.resetFailedLoginAttempts();
        userRepository.save(user);

        AuthSession session = AuthSession.create(user.getId());
        AuthSession savedSession = authSessionRepository.save(session);

        return new LoginResult(
                savedSession.getToken(),
                user.getId(),
                user.getEmail().value(),
                user.getRole(),
                savedSession.getExpiresAt()
        );
    }
}