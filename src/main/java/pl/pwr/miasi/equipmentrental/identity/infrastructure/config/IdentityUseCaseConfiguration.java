package pl.pwr.miasi.equipmentrental.identity.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.LoginUserUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.RegisterUserUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.AuthSessionRepository;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.PasswordHasher;
import pl.pwr.miasi.equipmentrental.identity.application.port.out.UserRepository;
import pl.pwr.miasi.equipmentrental.identity.application.service.LoginUserService;
import pl.pwr.miasi.equipmentrental.identity.application.service.RegisterUserService;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.identity.application.port.in.LockUserAccountUseCase;
import pl.pwr.miasi.equipmentrental.identity.application.service.LockUserAccountService;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;

@Configuration
public class IdentityUseCaseConfiguration {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            EventPublisher eventPublisher
    ) {
        return new RegisterUserService(
                userRepository,
                passwordHasher,
                eventPublisher
        );
    }

    @Bean
    public LoginUserUseCase loginUserUseCase(
            UserRepository userRepository,
            AuthSessionRepository authSessionRepository,
            PasswordHasher passwordHasher
    ) {
        return new LoginUserService(
                userRepository,
                authSessionRepository,
                passwordHasher
        );
    }

    @Bean
    LockUserAccountUseCase lockUserAccountUseCase(
            UserRepository userRepository,
            EventPublisher eventPublisher
    ) {
        return new LockUserAccountService(userRepository, eventPublisher);
    }
}