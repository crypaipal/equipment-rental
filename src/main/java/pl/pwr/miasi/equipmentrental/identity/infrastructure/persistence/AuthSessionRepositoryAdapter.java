package pl.pwr.miasi.equipmentrental.identity.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.AuthSessionRepository;
import pl.pwr.miasi.equipmentrental.identity.domain.AuthSession;

@Repository
public class AuthSessionRepositoryAdapter implements AuthSessionRepository {

    private final AuthSessionSpringDataRepository springDataRepository;

    public AuthSessionRepositoryAdapter(AuthSessionSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public AuthSession save(AuthSession session) {
        AuthSessionJpaEntity entity = new AuthSessionJpaEntity(
                session.getToken(),
                session.getUserId(),
                session.getCreatedAt(),
                session.getExpiresAt()
        );

        AuthSessionJpaEntity savedEntity = springDataRepository.save(entity);

        return new AuthSession(
                savedEntity.getToken(),
                savedEntity.getUserId(),
                savedEntity.getCreatedAt(),
                savedEntity.getExpiresAt()
        );
    }
}