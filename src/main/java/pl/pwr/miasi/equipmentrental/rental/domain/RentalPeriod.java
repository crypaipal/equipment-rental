package pl.pwr.miasi.equipmentrental.rental.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;

public record RentalPeriod(
        Instant from,
        Instant to
) {
    public RentalPeriod {
        if (from == null) {
            throw new BusinessException("Rental period start cannot be null");
        }

        if (to == null) {
            throw new BusinessException("Rental period end cannot be null");
        }

        if (!to.isAfter(from)) {
            throw new BusinessException("Rental period end must be after start");
        }
    }

    public boolean overlaps(RentalPeriod other) {
        return from.isBefore(other.to()) && to.isAfter(other.from());
    }
}