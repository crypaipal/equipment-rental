package pl.pwr.miasi.equipmentrental.identity.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.pwr.miasi.equipmentrental.identity.domain.Email;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.identity.domain.User;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRepositoryAdapterTest {

    @Test
    void savesUserByMappingDomainToJpaEntityAndBack() {
        UserSpringDataRepository springDataRepository = mock(UserSpringDataRepository.class);
        UserRepositoryAdapter adapter = new UserRepositoryAdapter(springDataRepository);
        Instant lockedUntil = Instant.now().plusSeconds(3600);
        User user = new User(
                UUID.randomUUID(),
                "Jan",
                "Kowalski",
                new Email("USER@example.com"),
                "hashed:secret",
                Role.BORROWER,
                lockedUntil,
                "Late return",
                2
        );
        when(springDataRepository.save(any(UserJpaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = adapter.save(user);

        ArgumentCaptor<UserJpaEntity> entityCaptor = ArgumentCaptor.forClass(UserJpaEntity.class);
        verify(springDataRepository).save(entityCaptor.capture());
        UserJpaEntity entity = entityCaptor.getValue();
        assertThat(entity.getId()).isEqualTo(user.getId());
        assertThat(entity.getEmail()).isEqualTo("user@example.com");
        assertThat(entity.getLockedUntil()).isEqualTo(lockedUntil);
        assertThat(entity.getLockReason()).isEqualTo("Late return");
        assertThat(entity.getFailedLoginAttempts()).isEqualTo(2);

        assertThat(savedUser.getId()).isEqualTo(user.getId());
        assertThat(savedUser.getEmail().value()).isEqualTo("user@example.com");
        assertThat(savedUser.getLockReason()).isEqualTo("Late return");
        assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(2);
    }

    @Test
    void findsUserByNormalizedEmailAndMapsEntityToDomain() {
        UserSpringDataRepository springDataRepository = mock(UserSpringDataRepository.class);
        UserRepositoryAdapter adapter = new UserRepositoryAdapter(springDataRepository);
        UUID userId = UUID.randomUUID();
        UserJpaEntity entity = new UserJpaEntity(
                userId,
                "Anna",
                "Nowak",
                "anna.nowak@example.com",
                "hashed:secret",
                Role.LAB_ASSISTANT,
                null,
                null,
                0
        );
        when(springDataRepository.findByEmailNormalized("anna.nowak@example.com"))
                .thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findByEmail(new Email(" ANNA.NOWAK@example.com "));

        verify(springDataRepository).findByEmailNormalized("anna.nowak@example.com");
        assertThat(result).isPresent();
        User user = result.orElseThrow();
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getFirstName()).isEqualTo("Anna");
        assertThat(user.getLastName()).isEqualTo("Nowak");
        assertThat(user.getEmail().value()).isEqualTo("anna.nowak@example.com");
        assertThat(user.getRole()).isEqualTo(Role.LAB_ASSISTANT);
    }
}
