package pl.pwr.miasi.equipmentrental.identity.application.service;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.identity.application.command.LockUserAccountCommand;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;
import pl.pwr.miasi.equipmentrental.identity.application.result.UserResult;
import pl.pwr.miasi.equipmentrental.identity.domain.Email;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.identity.domain.User;
import pl.pwr.miasi.equipmentrental.identity.domain.events.UserBlockedEvent;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;
import pl.pwr.miasi.equipmentrental.shared.exception.NotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LockUserAccountServiceTest {

    @Test
    void locksUserAccountAndPublishesUserBlockedEvent() {
        User user = activeUser();
        FakeUserRepository userRepository = new FakeUserRepository(user);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        LockUserAccountService service = new LockUserAccountService(userRepository, eventPublisher);

        Instant beforeLock = Instant.now();
        UserResult result = service.lockUserAccount(new LockUserAccountCommand(
                user.getId(),
                "Late return"
        ));

        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.lockedUntil()).isAfter(beforeLock);
        assertThat(result.lockReason()).isEqualTo("Late return");
        assertThat(userRepository.savedUser).isSameAs(user);
        assertThat(user.getLockedUntil()).isEqualTo(result.lockedUntil());
        assertThat(user.getLockReason()).isEqualTo("Late return");

        assertThat(eventPublisher.events).singleElement().isInstanceOf(UserBlockedEvent.class);
        UserBlockedEvent event = (UserBlockedEvent) eventPublisher.events.getFirst();
        assertThat(event.userId()).isEqualTo(user.getId());
        assertThat(event.lockedUntil()).isEqualTo(result.lockedUntil());
        assertThat(event.reason()).isEqualTo("Late return");
    }

    @Test
    void usesDefaultReasonWhenReasonIsBlank() {
        User user = activeUser();
        FakeUserRepository userRepository = new FakeUserRepository(user);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        LockUserAccountService service = new LockUserAccountService(userRepository, eventPublisher);

        UserResult result = service.lockUserAccount(new LockUserAccountCommand(user.getId(), " "));

        assertThat(result.lockReason()).isEqualTo("Rental returned after expected return date");
        assertThat(user.getLockReason()).isEqualTo("Rental returned after expected return date");

        assertThat(eventPublisher.events).singleElement().isInstanceOf(UserBlockedEvent.class);
        UserBlockedEvent event = (UserBlockedEvent) eventPublisher.events.getFirst();
        assertThat(event.reason()).isEqualTo("Rental returned after expected return date");
    }

    @Test
    void doesNotSaveOrPublishEventWhenUserIsAlreadyLocked() {
        Instant lockedUntil = Instant.now().plusSeconds(900);
        User user = lockedUser(lockedUntil, "Previous lock");
        FakeUserRepository userRepository = new FakeUserRepository(user);
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        LockUserAccountService service = new LockUserAccountService(userRepository, eventPublisher);

        UserResult result = service.lockUserAccount(new LockUserAccountCommand(
                user.getId(),
                "Late return"
        ));

        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.lockedUntil()).isEqualTo(lockedUntil);
        assertThat(result.lockReason()).isEqualTo("Previous lock");
        assertThat(userRepository.savedUser).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    @Test
    void rejectsMissingUserWithoutSavingOrPublishingEvent() {
        FakeUserRepository userRepository = new FakeUserRepository();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        LockUserAccountService service = new LockUserAccountService(userRepository, eventPublisher);

        assertThatThrownBy(() -> service.lockUserAccount(new LockUserAccountCommand(
                UUID.randomUUID(),
                "Late return"
        )))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");

        assertThat(userRepository.savedUser).isNull();
        assertThat(eventPublisher.events).isEmpty();
    }

    private static User activeUser() {
        return new User(
                UUID.randomUUID(),
                "Jan",
                "Kowalski",
                new Email("user@example.com"),
                "hashed:secret",
                Role.BORROWER,
                null,
                null,
                0
        );
    }

    private static User lockedUser(Instant lockedUntil, String lockReason) {
        return new User(
                UUID.randomUUID(),
                "Jan",
                "Kowalski",
                new Email("user@example.com"),
                "hashed:secret",
                Role.BORROWER,
                lockedUntil,
                lockReason,
                0
        );
    }

    private static class FakeUserRepository implements UserRepository {
        private final Map<UUID, User> users = new HashMap<>();
        private User savedUser;

        private FakeUserRepository(User... users) {
            for (User user : users) {
                this.users.put(user.getId(), user);
            }
        }

        @Override
        public User save(User user) {
            savedUser = user;
            users.put(user.getId(), user);
            return user;
        }

        @Override
        public boolean existsByEmail(Email email) {
            return users.values().stream()
                    .anyMatch(user -> user.getEmail().equals(email));
        }

        @Override
        public Optional<User> findByEmail(Email email) {
            return users.values().stream()
                    .filter(user -> user.getEmail().equals(email))
                    .findFirst();
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(users.get(id));
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
