package pl.pwr.miasi.equipmentrental.identity.application.service;

import org.springframework.stereotype.Service;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.IdentityFacade;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;
import pl.pwr.miasi.equipmentrental.identity.domain.User;

import java.util.UUID;

@Service
public class IdentityFacadeImpl implements IdentityFacade {
    private final UserRepository userRepository;

    public IdentityFacadeImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isUserBlocked(UUID userId) {
        return userRepository.findById(userId)
                .map(User::isLocked)
                .orElse(false);
    }
}