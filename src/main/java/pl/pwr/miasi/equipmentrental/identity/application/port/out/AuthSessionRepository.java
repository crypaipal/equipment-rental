package pl.pwr.miasi.equipmentrental.identity.application.port.out;

import pl.pwr.miasi.equipmentrental.identity.domain.AuthSession;

public interface AuthSessionRepository {

    AuthSession save(AuthSession session);
}