package pl.pwr.miasi.equipmentrental.rental.infrastructure.integration;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.UserAccessPort;

import java.util.UUID;

@Component
public class IdentityUserAccessAdapter implements UserAccessPort {

    private final UserRepository userRepository;

    public IdentityUserAccessAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean canUserRent(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> !user.isLocked())
                .orElse(false);
    }
}