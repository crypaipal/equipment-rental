package pl.pwr.miasi.equipmentrental.identity.application.command;

import pl.pwr.miasi.equipmentrental.identity.domain.Role;

public record RegisterUserCommand(
        String firstName,
        String lastName,
        String email,
        String password,
        Role role
) {
}