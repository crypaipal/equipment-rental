package pl.pwr.miasi.equipmentrental.identity.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.identity.application.command.LoginUserCommand;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.AuthSessionRepository;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.PasswordHasher;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;
import pl.pwr.miasi.equipmentrental.identity.application.result.LoginResult;
import pl.pwr.miasi.equipmentrental.identity.domain.AuthSession;
import pl.pwr.miasi.equipmentrental.identity.domain.Email;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.identity.domain.User;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginUserServiceTest {

    @Test
    void logsInUserWithValidPasswordAndCreatesSession() {
        User user = userWithFailedAttempts(2);
        FakeUserRepository userRepository = new FakeUserRepository(user);
        RecordingAuthSessionRepository authSessionRepository = new RecordingAuthSessionRepository();
        LoginUserService service = new LoginUserService(
                userRepository,
                authSessionRepository,
                new PrefixPasswordHasher()
        );

        LoginResult result = service.login(new LoginUserCommand("USER@example.com", "secret"));

        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.role()).isEqualTo(Role.BORROWER);
        assertThat(result.token()).isEqualTo(authSessionRepository.savedSession.getToken());
        assertThat(result.expiresAt()).isEqualTo(authSessionRepository.savedSession.getExpiresAt());
        assertThat(authSessionRepository.savedSession.getUserId()).isEqualTo(user.getId());
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(userRepository.savedUser).isSameAs(user);
    }

    @Test
    void rejectsInvalidPasswordAndRecordsFailedLoginAttempt() {
        User user = activeUser();
        FakeUserRepository userRepository = new FakeUserRepository(user);
        RecordingAuthSessionRepository authSessionRepository = new RecordingAuthSessionRepository();
        LoginUserService service = new LoginUserService(
                userRepository,
                authSessionRepository,
                new PrefixPasswordHasher()
        );

        assertThatThrownBy(() -> service.login(new LoginUserCommand("user@example.com", "wrong")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Invalid email or password");

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getAccountLock()).isNull();
        assertThat(userRepository.savedUser).isSameAs(user);
        assertThat(authSessionRepository.savedSession).isNull();
    }

    @Test
    void locksUserAfterThirdInvalidPassword() {
        User user = userWithFailedAttempts(2);
        FakeUserRepository userRepository = new FakeUserRepository(user);
        RecordingAuthSessionRepository authSessionRepository = new RecordingAuthSessionRepository();
        LoginUserService service = new LoginUserService(
                userRepository,
                authSessionRepository,
                new PrefixPasswordHasher()
        );

        assertThatThrownBy(() -> service.login(new LoginUserCommand("user@example.com", "wrong")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Invalid email or password");

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isAfter(Instant.now());
        assertThat(user.getLockReason()).isEqualTo("Too many failed login attempts");
        assertThat(userRepository.savedUser).isSameAs(user);
        assertThat(authSessionRepository.savedSession).isNull();
    }

    @Test
    void rejectsLockedUserBeforeCheckingPassword() {
        User user = activeUser();
        user.lockAccount(Instant.now().plusSeconds(900), "Manual lock");
        FakeUserRepository userRepository = new FakeUserRepository(user);
        RecordingAuthSessionRepository authSessionRepository = new RecordingAuthSessionRepository();
        PrefixPasswordHasher passwordHasher = new PrefixPasswordHasher();
        LoginUserService service = new LoginUserService(
                userRepository,
                authSessionRepository,
                passwordHasher
        );

        assertThatThrownBy(() -> service.login(new LoginUserCommand("user@example.com", "secret")))
                .isInstanceOf(BusinessException.class)
                .hasMessageStartingWith("User account is locked until ");

        assertThat(passwordHasher.hashCalls).isZero();
        assertThat(userRepository.savedUser).isNull();
        assertThat(authSessionRepository.savedSession).isNull();
    }

    private static User activeUser() {
        return userWithFailedAttempts(0);
    }

    private static User userWithFailedAttempts(int failedLoginAttempts) {
        return new User(
                UUID.randomUUID(),
                "Jan",
                "Kowalski",
                new Email("user@example.com"),
                "hashed:secret",
                Role.BORROWER,
                null,
                null,
                failedLoginAttempts
        );
    }

    private static class FakeUserRepository implements UserRepository {
        private final Map<Email, User> users = new HashMap<>();
        private User savedUser;

        private FakeUserRepository(User user) {
            users.put(user.getEmail(), user);
        }

        @Override
        public User save(User user) {
            savedUser = user;
            users.put(user.getEmail(), user);
            return user;
        }

        @Override
        public boolean existsByEmail(Email email) {
            return users.containsKey(email);
        }

        @Override
        public Optional<User> findByEmail(Email email) {
            return Optional.ofNullable(users.get(email));
        }

        @Override
        public Optional<User> findById(UUID id) {
            return users.values().stream()
                    .filter(user -> user.getId().equals(id))
                    .findFirst();
        }
    }

    private static class RecordingAuthSessionRepository implements AuthSessionRepository {
        private AuthSession savedSession;

        @Override
        public AuthSession save(AuthSession session) {
            savedSession = session;
            return session;
        }
    }

    private static class PrefixPasswordHasher implements PasswordHasher {
        private int hashCalls;

        @Override
        public String hash(String rawPassword) {
            hashCalls++;
            return "hashed:" + rawPassword;
        }
    }
}
