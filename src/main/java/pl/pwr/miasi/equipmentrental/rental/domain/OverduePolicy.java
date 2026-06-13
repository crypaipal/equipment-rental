package pl.pwr.miasi.equipmentrental.rental.domain;

import java.util.UUID;

public class OverduePolicy {

    public boolean canCheckoutWithActiveRentals(UUID studentId) {
        return studentId != null;
    }

    public boolean isReturnOverdue(Rental rental) {
        return rental != null && rental.isOverdue();
    }
}