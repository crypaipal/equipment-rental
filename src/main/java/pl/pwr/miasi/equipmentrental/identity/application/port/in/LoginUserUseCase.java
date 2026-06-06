package pl.pwr.miasi.equipmentrental.identity.application.port.in;

import pl.pwr.miasi.equipmentrental.identity.application.command.LoginUserCommand;
import pl.pwr.miasi.equipmentrental.identity.application.result.LoginResult;

public interface LoginUserUseCase {

    LoginResult login(LoginUserCommand command);
}