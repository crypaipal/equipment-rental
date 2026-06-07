package pl.pwr.miasi.equipmentrental.identity.application.command;

import java.util.UUID;

public record LockUserAccountCommand(
        UUID userId,
        String reason
) {
}