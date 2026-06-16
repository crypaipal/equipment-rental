package pl.pwr.miasi.equipmentrental.rental.infrastructure.integration;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.IdentityFacade;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.UserAccessPort;

import java.util.UUID;

@Component
public class IdentityUserAccessAdapter implements UserAccessPort {

    private final IdentityFacade identityFacade;

    public IdentityUserAccessAdapter(IdentityFacade identityFacade) {
        this.identityFacade = identityFacade;
    }

    @Override
    public boolean canUserRent(UUID userId) {
        return !identityFacade.isUserBlocked(userId);
    }
}