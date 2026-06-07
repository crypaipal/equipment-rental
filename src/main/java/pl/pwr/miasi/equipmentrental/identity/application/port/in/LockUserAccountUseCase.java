package pl.pwr.miasi.equipmentrental.identity.application.port.in;

import pl.pwr.miasi.equipmentrental.identity.application.command.LockUserAccountCommand;
import pl.pwr.miasi.equipmentrental.identity.application.result.UserResult;

public interface LockUserAccountUseCase {

    UserResult lockUserAccount(LockUserAccountCommand command);
}