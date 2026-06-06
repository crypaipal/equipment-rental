package pl.pwr.miasi.equipmentrental.rental.application.command;

import java.util.UUID;

public record ReviewReservationCommand(
        UUID reservationId,
        boolean approved,
        String rejectionReason
) {
}