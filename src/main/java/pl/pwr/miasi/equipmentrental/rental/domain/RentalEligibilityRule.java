package pl.pwr.miasi.equipmentrental.rental.domain;

import pl.pwr.miasi.equipmentrental.rental.application.port.out.UserAccessPort;

import java.util.UUID;

public class RentalEligibilityRule {

    private final UserAccessPort userAccessPort;

    public RentalEligibilityRule(UserAccessPort userAccessPort) {
        this.userAccessPort = userAccessPort;
    }

    public boolean isStudentEligible(UUID studentId) {
        if (studentId == null) {
            return false;
        }

        return userAccessPort.canUserRent(studentId);
    }
}