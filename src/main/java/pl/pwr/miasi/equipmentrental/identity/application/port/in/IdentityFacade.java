package pl.pwr.miasi.equipmentrental.identity.application.port.in;

import java.util.UUID;

public interface IdentityFacade {
    boolean isUserBlocked(UUID userId);
}