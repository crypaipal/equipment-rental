package pl.pwr.miasi.equipmentrental.identity.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.identity.application.command.RegisterUserCommand;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.PasswordHasher;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;
import pl.pwr.miasi.equipmentrental.identity.application.result.UserResult;
import pl.pwr.miasi.equipmentrental.identity.domain.Email;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.identity.domain.User;
import pl.pwr.miasi.equipmentrental.identity.domain.events.UserRegisteredEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserServiceTest {

    @Test
    void registersUserAndPublishesUserRegisteredEvent() {
        FakeUserRepository userRepository = new FakeUserRepository();
        PrefixPasswordHasher passwordHasher = new PrefixPasswordHasher();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RegisterUserService service = new RegisterUserService(userRepository, passwordHasher, eventPublisher);

        UserResult result = service.register(new RegisterUserCommand(
                "Jan",
                "Kowalski",
                "JAN.KOWALSKI@example.com",
                "secret123",
                Role.LAB_ASSISTANT
        ));

        assertThat(result.id()).isEqualTo(userRepository.savedUser.getId());
        assertThat(result.firstName()).isEqualTo("Jan");
        assertThat(result.lastName()).isEqualTo("Kowalski");
        assertThat(result.email()).isEqualTo("jan.kowalski@example.com");
        assertThat(result.role()).isEqualTo(Role.LAB_ASSISTANT);
        assertThat(result.lockedUntil()).isNull();
        assertThat(result.lockReason()).isNull();

        assertThat(passwordHasher.hashCalls).isEqualTo(1);
        assertThat(userRepository.savedUser.getPasswordHash()).isEqualTo("hashed:secret123");

        assertThat(eventPublisher.events).singleElement().isInstanceOf(UserRegisteredEvent.class);
        UserRegisteredEvent event = (UserRegisteredEvent) eventPublisher.events.getFirst();
        assertThat(event.userId()).isEqualTo(result.id());
        assertThat(event.email()).isEqualTo("jan.kowalski@example.com");
        assertThat(event.role()).isEqualTo(Role.LAB_ASSISTANT);
    }

    @Test
    void registersBorrowerWhenRoleIsNotProvided() {
        FakeUserRepository userRepository = new FakeUserRepository();
        RegisterUserService service = new RegisterUserService(
                userRepository,
                new PrefixPasswordHasher(),
                new RecordingEventPublisher()
        );

        UserResult result = service.register(new RegisterUserCommand(
                "Anna",
                "Nowak",
                "anna.nowak@example.com",
                "secret123",
                null
        ));

        assertThat(result.role()).isEqualTo(Role.BORROWER);
        assertThat(userRepository.savedUser.getRole()).isEqualTo(Role.BORROWER);
    }

    @Test
    void rejectsDuplicateEmailWithoutSavingUserOrPublishingEvent() {
        User existingUser = user("existing@example.com");
        FakeUserRepository userRepository = new FakeUserRepository(existingUser);
        PrefixPasswordHasher passwordHasher = new PrefixPasswordHasher();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RegisterUserService service = new RegisterUserService(userRepository, passwordHasher, eventPublisher);

        assertThatThrownBy(() -> service.register(new RegisterUserCommand(
                "Jan",
                "Kowalski",
                "EXISTING@example.com",
                "secret123",
                Role.BORROWER
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("User with this email already exists");

        assertThat(passwordHasher.hashCalls).isZero();
        assertThat(userRepository.savedUser).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    @Test
    void rejectsPasswordShorterThanSixCharacters() {
        FakeUserRepository userRepository = new FakeUserRepository();
        PrefixPasswordHasher passwordHasher = new PrefixPasswordHasher();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        RegisterUserService service = new RegisterUserService(userRepository, passwordHasher, eventPublisher);

        assertThatThrownBy(() -> service.register(new RegisterUserCommand(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "12345",
                Role.BORROWER
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Password must have at least 6 characters");

        assertThat(passwordHasher.hashCalls).isZero();
        assertThat(userRepository.savedUser).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    private static User user(String email) {
        return User.register(
                "Existing",
                "User",
                new Email(email),
                "hashed:secret123",
                Role.BORROWER
        );
    }

    private static class FakeUserRepository implements UserRepository {
        private final Map<Email, User> users = new HashMap<>();
        private User savedUser;

        private FakeUserRepository(User... users) {
            for (User user : users) {
                this.users.put(user.getEmail(), user);
            }
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

    private static class PrefixPasswordHasher implements PasswordHasher {
        private int hashCalls;

        @Override
        public String hash(String rawPassword) {
            hashCalls++;
            return "hashed:" + rawPassword;
        }
    }

    private static class RecordingEventPublisher implements EventPublisher {
        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }
    }
}
