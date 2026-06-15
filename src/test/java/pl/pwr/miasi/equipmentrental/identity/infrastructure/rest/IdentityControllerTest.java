package pl.pwr.miasi.equipmentrental.identity.infrastructure.rest;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.identity.application.command.LoginUserCommand;
import pl.pwr.miasi.equipmentrental.identity.application.command.RegisterUserCommand;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.LoginUserUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.RegisterUserUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.result.LoginResult;
import pl.pwr.miasi.equipmentrental.identity.application.result.UserResult;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityControllerTest {

    @Test
    void mapsRegisterRequestToCommandAndUserResponse() {
        RecordingRegisterUserUseCase registerUserUseCase = new RecordingRegisterUserUseCase();
        IdentityController controller = new IdentityController(
                registerUserUseCase,
                new RecordingLoginUserUseCase()
        );
        UUID userId = UUID.randomUUID();
        registerUserUseCase.result = new UserResult(
                userId,
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                Role.BORROWER,
                null,
                null
        );

        UserResponse response = controller.register(new RegisterUserRequest(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "secret123",
                Role.BORROWER
        ));

        assertThat(registerUserUseCase.command.firstName()).isEqualTo("Jan");
        assertThat(registerUserUseCase.command.lastName()).isEqualTo("Kowalski");
        assertThat(registerUserUseCase.command.email()).isEqualTo("jan.kowalski@example.com");
        assertThat(registerUserUseCase.command.password()).isEqualTo("secret123");
        assertThat(registerUserUseCase.command.role()).isEqualTo(Role.BORROWER);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("jan.kowalski@example.com");
        assertThat(response.role()).isEqualTo(Role.BORROWER);
    }

    @Test
    void mapsLoginRequestToCommandAndLoginResponse() {
        RecordingLoginUserUseCase loginUserUseCase = new RecordingLoginUserUseCase();
        IdentityController controller = new IdentityController(
                new RecordingRegisterUserUseCase(),
                loginUserUseCase
        );
        UUID token = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(3600);
        loginUserUseCase.result = new LoginResult(
                token,
                userId,
                "jan.kowalski@example.com",
                Role.BORROWER,
                expiresAt
        );

        LoginResponse response = controller.login(new LoginRequest(
                "jan.kowalski@example.com",
                "secret123"
        ));

        assertThat(loginUserUseCase.command.email()).isEqualTo("jan.kowalski@example.com");
        assertThat(loginUserUseCase.command.password()).isEqualTo("secret123");
        assertThat(response.token()).isEqualTo(token);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }

    private static class RecordingRegisterUserUseCase implements RegisterUserUseCase {
        private RegisterUserCommand command;
        private UserResult result;

        @Override
        public UserResult register(RegisterUserCommand command) {
            this.command = command;
            return result;
        }
    }

    private static class RecordingLoginUserUseCase implements LoginUserUseCase {
        private LoginUserCommand command;
        private LoginResult result;

        @Override
        public LoginResult login(LoginUserCommand command) {
            this.command = command;
            return result;
        }
    }
}
