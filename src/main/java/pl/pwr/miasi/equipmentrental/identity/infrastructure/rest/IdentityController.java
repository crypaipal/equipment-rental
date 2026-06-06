package pl.pwr.miasi.equipmentrental.identity.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.identity.application.command.LoginUserCommand;
import pl.pwr.miasi.equipmentrental.identity.application.command.RegisterUserCommand;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.LoginUserUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.RegisterUserUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.result.LoginResult;
import pl.pwr.miasi.equipmentrental.identity.application.result.UserResult;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    public IdentityController(
            RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        UserResult result = registerUserUseCase.register(
                new RegisterUserCommand(
                        request.firstName(),
                        request.lastName(),
                        request.email(),
                        request.password(),
                        request.role()
                )
        );

        return new UserResponse(
                result.id(),
                result.firstName(),
                result.lastName(),
                result.email(),
                result.role(),
                result.lockedUntil(),
                result.lockReason()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUserUseCase.login(
                new LoginUserCommand(
                        request.email(),
                        request.password()
                )
        );

        return new LoginResponse(
                result.token(),
                result.userId(),
                result.email(),
                result.role(),
                result.expiresAt()
        );
    }
}