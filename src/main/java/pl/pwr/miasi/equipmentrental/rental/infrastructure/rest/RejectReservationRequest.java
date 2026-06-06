package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

public record RejectReservationRequest(
        String rejectionReason
) {
}