package pl.pwr.miasi.equipmentrental.identity.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final Email email;
    private final String passwordHash;
    private final Role role;
    private Instant lockedUntil;
    private String lockReason;

    public User(
            UUID id,
            String firstName,
            String lastName,
            Email email,
            String passwordHash,
            Role role,
            Instant lockedUntil,
            String lockReason
    ) {
        if (id == null) {
            throw new BusinessException("User id cannot be null");
        }

        if (firstName == null || firstName.isBlank()) {
            throw new BusinessException("First name cannot be empty");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new BusinessException("Last name cannot be empty");
        }

        if (email == null) {
            throw new BusinessException("Email cannot be null");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new BusinessException("Password hash cannot be empty");
        }

        if (role == null) {
            throw new BusinessException("Role cannot be null");
        }

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.lockedUntil = lockedUntil;
        this.lockReason = lockReason;
    }

    public static User register(
            String firstName,
            String lastName,
            Email email,
            String passwordHash,
            Role role
    ) {
        return new User(
                UUID.randomUUID(),
                firstName,
                lastName,
                email,
                passwordHash,
                role,
                null,
                null
        );
    }

    public void lockAccount(Instant lockedUntil, String reason) {
        if (lockedUntil == null) {
            throw new BusinessException("Lock expiration date cannot be null");
        }

        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Lock reason cannot be empty");
        }

        this.lockedUntil = lockedUntil;
        this.lockReason = reason;
    }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
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

    public Email getEmail() {
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
}