package pl.pwr.miasi.equipmentrental.identity.application.port.out;

import pl.pwr.miasi.equipmentrental.identity.domain.Email;
import pl.pwr.miasi.equipmentrental.identity.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    boolean existsByEmail(Email email);

    Optional<User> findByEmail(Email email);
}