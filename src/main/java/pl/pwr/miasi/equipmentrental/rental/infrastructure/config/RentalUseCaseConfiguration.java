package pl.pwr.miasi.equipmentrental.rental.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.RequestReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.UserAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.service.RequestReservationService;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;

@Configuration
public class RentalUseCaseConfiguration {

    @Bean
    public RequestReservationUseCase requestReservationUseCase(
            ReservationRepository reservationRepository,
            UserAccessPort userAccessPort,
            InventoryAssetAccessPort inventoryAssetAccessPort,
            EventPublisher eventPublisher
    ) {
        return new RequestReservationService(
                reservationRepository,
                userAccessPort,
                inventoryAssetAccessPort,
                eventPublisher
        );
    }
}