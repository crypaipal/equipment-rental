package pl.pwr.miasi.equipmentrental.identity.application.port.in;

import pl.pwr.miasi.equipmentrental.identity.application.command.RegisterUserCommand;
import pl.pwr.miasi.equipmentrental.identity.application.result.UserResult;

public interface RegisterUserUseCase {

    UserResult register(RegisterUserCommand command);
}