package pl.pwr.miasi.equipmentrental.identity.infrastructure.security;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.persistence.AuthSessionSpringDataRepository;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.persistence.UserSpringDataRepository;
import pl.pwr.miasi.equipmentrental.shared.exception.UnauthorizedException;

import java.time.Instant;
import java.util.UUID;

@Component
public class AuthenticatedUserResolver {

    private final AuthSessionSpringDataRepository authSessionRepository;
    private final UserSpringDataRepository userRepository;

    public AuthenticatedUserResolver(
            AuthSessionSpringDataRepository authSessionRepository,
            UserSpringDataRepository userRepository
    ) {
        this.authSessionRepository = authSessionRepository;
        this.userRepository = userRepository;
    }

    public AuthenticatedUser resolve(String authorizationHeader) {
        UUID token = extractToken(authorizationHeader);

        var session = authSessionRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired session token."));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Session token has expired.");
        }

        var user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user does not exist."));

        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }

    private UUID extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Authorization header is required.");
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header must use Bearer token.");
        }

        String rawToken = authorizationHeader.substring("Bearer ".length()).trim();

        try {
            return UUID.fromString(rawToken);
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("Invalid authorization token format.");
        }
    }
}