package pl.pwr.miasi.equipmentrental.identity.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;
import pl.pwr.miasi.equipmentrental.identity.domain.Email;
import pl.pwr.miasi.equipmentrental.identity.domain.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserSpringDataRepository springDataRepository;

    public UserRepositoryAdapter(UserSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity savedEntity = springDataRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springDataRepository.existsByEmailNormalized(email.value());
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataRepository.findByEmailNormalized(email.value())
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(this::toDomain);
    }

    private UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail().value(),
                user.getPasswordHash(),
                user.getRole(),
                user.getLockedUntil(),
                user.getLockReason(),
                user.getFailedLoginAttempts()
        );
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                new Email(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getLockedUntil(),
                entity.getLockReason(),
                entity.getFailedLoginAttempts()
        );
    }
}