package pl.pwr.miasi.equipmentrental.identity.infrastructure.rest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;

public record RegisterUserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @Size(min = 6) String password,
        Role role
) {
}