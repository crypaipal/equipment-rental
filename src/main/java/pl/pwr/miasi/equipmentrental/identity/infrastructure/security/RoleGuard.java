package pl.pwr.miasi.equipmentrental.identity.infrastructure.security;

import org.springframework.stereotype.Component;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.shared.exception.ForbiddenException;

import java.util.Arrays;

@Component
public class RoleGuard {

    public void requireAnyRole(AuthenticatedUser user, Role... allowedRoles) {
        boolean allowed = Arrays.asList(allowedRoles).contains(user.role());

        if (!allowed) {
            throw new ForbiddenException("You do not have permission to perform this operation.");
        }
    }
}