package pl.pwr.miasi.equipmentrental.identity.infrastructure.persistence;

import jakarta.persistence.*;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "lock_reason", length = 500)
    private String lockReason;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    protected UserJpaEntity() {
    }

    public UserJpaEntity(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String passwordHash,
            Role role,
            Instant lockedUntil,
            String lockReason,
            int failedLoginAttempts
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.lockedUntil = lockedUntil;
        this.lockReason = lockReason;
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public String getLockReason() {
        return lockReason;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
}