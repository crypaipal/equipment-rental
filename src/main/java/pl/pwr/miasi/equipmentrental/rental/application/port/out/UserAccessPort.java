package pl.pwr.miasi.equipmentrental.rental.application.port.out;

import java.util.UUID;

public interface UserAccessPort {

    boolean canUserRent(UUID userId);

    void blockUserDueToOverdue(UUID userId, String reason);
}