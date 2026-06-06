package pl.pwr.miasi.equipmentrental.rental.application.command;

import java.util.UUID;

public record CancelReservationCommand(
        UUID reservationId
) {
}