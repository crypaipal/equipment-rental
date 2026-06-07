package pl.pwr.miasi.equipmentrental.identity.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

public record Email(String value) {

    public Email {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Email cannot be empty");
        }

        value = value.trim().toLowerCase();

        if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BusinessException("Invalid email format");
        }
    }
}