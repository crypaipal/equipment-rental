package pl.pwr.miasi.equipmentrental.identity.application.command;

public record LoginUserCommand(
        String email,
        String password
) {
}