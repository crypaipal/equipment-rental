package pl.pwr.miasi.equipmentrental.identity.application.port.in;

import org.springframework.stereotype.Service;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;

import java.util.UUID;

@Service
public class IdentityFacade {

    private final UserRepository userRepository;

    public IdentityFacade(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isUserBlocked(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.isLocked())
                .orElse(true);
    }
}