package pl.pwr.miasi.equipmentrental.rental.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.CheckoutEquipmentUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.RequestReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.ReturnEquipmentUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.ReviewReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.InventoryAssetAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.RentalRepository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.ReservationRepository;
import pl.pwr.miasi.equipmentrental.rental.application.port.out.UserAccessPort;
import pl.pwr.miasi.equipmentrental.rental.application.service.CheckoutEquipmentService;
import pl.pwr.miasi.equipmentrental.rental.application.service.RequestReservationService;
import pl.pwr.miasi.equipmentrental.rental.application.service.ReturnEquipmentService;
import pl.pwr.miasi.equipmentrental.rental.application.service.ReviewReservationService;
import pl.pwr.miasi.equipmentrental.shared.application.EventPublisher;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.CancelReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.service.CancelReservationService;

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

    @Bean
    public ReviewReservationUseCase reviewReservationUseCase(
            ReservationRepository reservationRepository,
            EventPublisher eventPublisher
    ) {
        return new ReviewReservationService(
                reservationRepository,
                eventPublisher
        );
    }

    @Bean
    public CheckoutEquipmentUseCase checkoutEquipmentUseCase(
            ReservationRepository reservationRepository,
            RentalRepository rentalRepository,
            InventoryAssetAccessPort inventoryAssetAccessPort,
            EventPublisher eventPublisher
    ) {
        return new CheckoutEquipmentService(
                reservationRepository,
                rentalRepository,
                inventoryAssetAccessPort,
                eventPublisher
        );
    }

    @Bean
    public ReturnEquipmentUseCase returnEquipmentUseCase(
            RentalRepository rentalRepository,
            InventoryAssetAccessPort inventoryAssetAccessPort,
            UserAccessPort userAccessPort,
            EventPublisher eventPublisher
    ) {
        return new ReturnEquipmentService(
                rentalRepository,
                inventoryAssetAccessPort,
                userAccessPort,
                eventPublisher
        );
    }

    @Bean
    public CancelReservationUseCase cancelReservationUseCase(
            ReservationRepository reservationRepository,
            EventPublisher eventPublisher
    ) {
        return new CancelReservationService(
                reservationRepository,
                eventPublisher
        );
    }
}