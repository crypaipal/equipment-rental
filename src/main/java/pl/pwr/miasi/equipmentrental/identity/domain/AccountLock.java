package pl.pwr.miasi.equipmentrental.identity.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;

public class AccountLock {

    private final Instant lockedUntil;
    private final String reason;

    public AccountLock(Instant lockedUntil, String reason) {
        if (lockedUntil == null) {
            throw new BusinessException("Lock expiration date cannot be null");
        }

        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Lock reason cannot be empty");
        }

        this.lockedUntil = lockedUntil;
        this.reason = reason;
    }

    public boolean isActive(Instant currentDate) {
        if (currentDate == null) {
            return false;
        }

        return lockedUntil.isAfter(currentDate);
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public String getReason() {
        return reason;
    }
}