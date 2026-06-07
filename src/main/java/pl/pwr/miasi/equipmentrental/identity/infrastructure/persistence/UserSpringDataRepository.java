package pl.pwr.miasi.equipmentrental.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserSpringDataRepository extends JpaRepository<UserJpaEntity, UUID> {

    @Query("""
            select count(u) > 0
            from UserJpaEntity u
            where lower(trim(u.email)) = lower(trim(:email))
            """)
    boolean existsByEmailNormalized(@Param("email") String email);

    @Query("""
            select u
            from UserJpaEntity u
            where lower(trim(u.email)) = lower(trim(:email))
            """)
    Optional<UserJpaEntity> findByEmailNormalized(@Param("email") String email);
}