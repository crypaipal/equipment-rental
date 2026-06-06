package pl.pwr.miasi.equipmentrental.identity.infrastructure.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.pwr.miasi.equipmentrental.identity.application.command.RegisterUserCommand;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.RegisterUserUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.result.UserResult;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final RegisterUserUseCase registerUserUseCase;

    public IdentityController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
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
}