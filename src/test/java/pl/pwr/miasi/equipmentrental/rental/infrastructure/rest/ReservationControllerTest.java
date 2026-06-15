package pl.pwr.miasi.equipmentrental.rental.infrastructure.rest;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.AuthenticatedUser;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.AuthenticatedUserResolver;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.RoleGuard;
import pl.pwr.miasi.equipmentrental.rental.application.command.CancelReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.command.CheckoutEquipmentCommand;
import pl.pwr.miasi.equipmentrental.rental.application.command.RequestReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.command.ReviewReservationCommand;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.CancelReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.CheckoutEquipmentUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.FindAllReservationsUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.RequestReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.port.in.ReviewReservationUseCase;
import pl.pwr.miasi.equipmentrental.rental.application.result.RentalResult;
import pl.pwr.miasi.equipmentrental.rental.application.result.ReservationResult;
import pl.pwr.miasi.equipmentrental.rental.domain.ReservationStatus;
import pl.pwr.miasi.equipmentrental.shared.exception.ForbiddenException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationControllerTest {

    @Test
    void mapsRequestReservationToCommandWhenBorrowerUsesOwnAccount() {
        UUID userId = UUID.randomUUID();
        StaticAuthenticatedUserResolver authenticatedUserResolver = authenticatedUserResolver(userId, Role.BORROWER);
        RecordingRequestReservationUseCase requestReservationUseCase = new RecordingRequestReservationUseCase();
        ReservationController controller = controller(
                requestReservationUseCase,
                new RecordingReviewReservationUseCase(),
                authenticatedUserResolver
        );
        UUID reservationId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        Instant periodFrom = Instant.now().plusSeconds(3600);
        Instant periodTo = periodFrom.plusSeconds(7200);
        Instant createdAt = Instant.now();
        requestReservationUseCase.result = new ReservationResult(
                reservationId,
                userId,
                assetId,
                periodFrom,
                periodTo,
                ReservationStatus.PENDING,
                null,
                createdAt
        );

        ReservationResponse response = controller.request(
                "Bearer token",
                new RequestReservationRequest(userId, assetId, periodFrom, periodTo)
        );

        assertThat(authenticatedUserResolver.authorizationHeader).isEqualTo("Bearer token");
        assertThat(requestReservationUseCase.command.userId()).isEqualTo(userId);
        assertThat(requestReservationUseCase.command.assetId()).isEqualTo(assetId);
        assertThat(requestReservationUseCase.command.periodFrom()).isEqualTo(periodFrom);
        assertThat(requestReservationUseCase.command.periodTo()).isEqualTo(periodTo);
        assertThat(response.id()).isEqualTo(reservationId);
        assertThat(response.status()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void rejectsReservationRequestForDifferentUserBeforeCallingUseCase() {
        StaticAuthenticatedUserResolver authenticatedUserResolver = authenticatedUserResolver(
                UUID.randomUUID(),
                Role.BORROWER
        );
        RecordingRequestReservationUseCase requestReservationUseCase = new RecordingRequestReservationUseCase();
        ReservationController controller = controller(
                requestReservationUseCase,
                new RecordingReviewReservationUseCase(),
                authenticatedUserResolver
        );

        assertThatThrownBy(() -> controller.request(
                "Bearer token",
                new RequestReservationRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        Instant.now().plusSeconds(3600),
                        Instant.now().plusSeconds(7200)
                )
        ))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You can create reservations only for your own account.");

        assertThat(requestReservationUseCase.command).isNull();
    }

    @Test
    void mapsApproveRequestToReviewCommandForLabAssistant() {
        StaticAuthenticatedUserResolver authenticatedUserResolver = authenticatedUserResolver(
                UUID.randomUUID(),
                Role.LAB_ASSISTANT
        );
        RecordingReviewReservationUseCase reviewReservationUseCase = new RecordingReviewReservationUseCase();
        ReservationController controller = controller(
                new RecordingRequestReservationUseCase(),
                reviewReservationUseCase,
                authenticatedUserResolver
        );
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        Instant periodFrom = Instant.now().plusSeconds(3600);
        Instant periodTo = periodFrom.plusSeconds(7200);
        reviewReservationUseCase.result = new ReservationResult(
                reservationId,
                userId,
                assetId,
                periodFrom,
                periodTo,
                ReservationStatus.APPROVED,
                null,
                Instant.now()
        );

        ReservationResponse response = controller.approve("Bearer token", reservationId);

        assertThat(reviewReservationUseCase.command.reservationId()).isEqualTo(reservationId);
        assertThat(reviewReservationUseCase.command.approved()).isTrue();
        assertThat(reviewReservationUseCase.command.rejectionReason()).isNull();
        assertThat(response.status()).isEqualTo(ReservationStatus.APPROVED);
    }

    private static ReservationController controller(
            RequestReservationUseCase requestReservationUseCase,
            ReviewReservationUseCase reviewReservationUseCase,
            AuthenticatedUserResolver authenticatedUserResolver
    ) {
        return new ReservationController(
                requestReservationUseCase,
                reviewReservationUseCase,
                new EmptyCheckoutEquipmentUseCase(),
                new EmptyCancelReservationUseCase(),
                new EmptyFindAllReservationsUseCase(),
                authenticatedUserResolver,
                new RoleGuard()
        );
    }

    private static StaticAuthenticatedUserResolver authenticatedUserResolver(UUID userId, Role role) {
        return new StaticAuthenticatedUserResolver(new AuthenticatedUser(
                userId,
                "user@example.com",
                role
        ));
    }

    private static class StaticAuthenticatedUserResolver extends AuthenticatedUserResolver {
        private final AuthenticatedUser user;
        private String authorizationHeader;

        private StaticAuthenticatedUserResolver(AuthenticatedUser user) {
            super(null, null);
            this.user = user;
        }

        @Override
        public AuthenticatedUser resolve(String authorizationHeader) {
            this.authorizationHeader = authorizationHeader;
            return user;
        }
    }

    private static class RecordingRequestReservationUseCase implements RequestReservationUseCase {
        private RequestReservationCommand command;
        private ReservationResult result;

        @Override
        public ReservationResult request(RequestReservationCommand command) {
            this.command = command;
            return result;
        }
    }

    private static class RecordingReviewReservationUseCase implements ReviewReservationUseCase {
        private ReviewReservationCommand command;
        private ReservationResult result;

        @Override
        public ReservationResult review(ReviewReservationCommand command) {
            this.command = command;
            return result;
        }
    }

    private static class EmptyCheckoutEquipmentUseCase implements CheckoutEquipmentUseCase {
        @Override
        public RentalResult checkout(CheckoutEquipmentCommand command) {
            return null;
        }
    }

    private static class EmptyCancelReservationUseCase implements CancelReservationUseCase {
        @Override
        public ReservationResult cancel(CancelReservationCommand command) {
            return null;
        }
    }

    private static class EmptyFindAllReservationsUseCase implements FindAllReservationsUseCase {
        @Override
        public List<ReservationResult> findAll() {
            return List.of();
        }
    }
}
