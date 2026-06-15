package pl.pwr.miasi.equipmentrental.inventory.infrastructure.rest;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.AuthenticatedUser;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.AuthenticatedUserResolver;
import pl.pwr.miasi.equipmentrental.identity.infrastructure.security.RoleGuard;
import pl.pwr.miasi.equipmentrental.inventory.application.command.ChangeAssetConditionCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.command.RegisterAssetCommand;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.ChangeAssetConditionUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.FindAllAssetsUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.port.in.RegisterAssetUseCase;
import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;
import pl.pwr.miasi.equipmentrental.inventory.domain.AssetCondition;
import pl.pwr.miasi.equipmentrental.shared.exception.ForbiddenException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetControllerTest {

    @Test
    void mapsRegisterRequestToCommandAndAssetResponseForAdmin() {
        StaticAuthenticatedUserResolver authenticatedUserResolver = authenticatedUserResolver(Role.SYSTEM_ADMIN);
        RecordingRegisterAssetUseCase registerAssetUseCase = new RecordingRegisterAssetUseCase();
        AssetController controller = new AssetController(
                registerAssetUseCase,
                new RecordingChangeAssetConditionUseCase(),
                new EmptyFindAllAssetsUseCase(),
                authenticatedUserResolver,
                new RoleGuard()
        );
        UUID assetId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        registerAssetUseCase.result = new AssetResult(
                assetId,
                modelId,
                "CAM-001",
                AssetCondition.OPERATIONAL,
                null
        );

        AssetResponse response = controller.register(
                "Bearer token",
                new RegisterAssetRequest(modelId, "CAM-001")
        );

        assertThat(authenticatedUserResolver.authorizationHeader).isEqualTo("Bearer token");
        assertThat(registerAssetUseCase.command.equipmentModelId()).isEqualTo(modelId);
        assertThat(registerAssetUseCase.command.inventoryTag()).isEqualTo("CAM-001");
        assertThat(response.id()).isEqualTo(assetId);
        assertThat(response.condition()).isEqualTo(AssetCondition.OPERATIONAL);
    }

    @Test
    void rejectsRegisterRequestForNonAdminBeforeCallingUseCase() {
        StaticAuthenticatedUserResolver authenticatedUserResolver = authenticatedUserResolver(Role.BORROWER);
        RecordingRegisterAssetUseCase registerAssetUseCase = new RecordingRegisterAssetUseCase();
        AssetController controller = new AssetController(
                registerAssetUseCase,
                new RecordingChangeAssetConditionUseCase(),
                new EmptyFindAllAssetsUseCase(),
                authenticatedUserResolver,
                new RoleGuard()
        );

        assertThatThrownBy(() -> controller.register(
                "Bearer token",
                new RegisterAssetRequest(UUID.randomUUID(), "CAM-001")
        ))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You do not have permission to perform this operation.");

        assertThat(registerAssetUseCase.command).isNull();
    }

    @Test
    void mapsChangeConditionRequestToCommandAndAssetResponseForLabAssistant() {
        StaticAuthenticatedUserResolver authenticatedUserResolver = authenticatedUserResolver(Role.LAB_ASSISTANT);
        RecordingChangeAssetConditionUseCase changeAssetConditionUseCase = new RecordingChangeAssetConditionUseCase();
        AssetController controller = new AssetController(
                new RecordingRegisterAssetUseCase(),
                changeAssetConditionUseCase,
                new EmptyFindAllAssetsUseCase(),
                authenticatedUserResolver,
                new RoleGuard()
        );
        UUID assetId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        changeAssetConditionUseCase.result = new AssetResult(
                assetId,
                modelId,
                "CAM-001",
                AssetCondition.DAMAGED,
                "Broken lens"
        );

        AssetResponse response = controller.changeCondition(
                "Bearer token",
                assetId,
                new ChangeAssetConditionRequest(AssetCondition.DAMAGED, "Broken lens")
        );

        assertThat(changeAssetConditionUseCase.command.assetId()).isEqualTo(assetId);
        assertThat(changeAssetConditionUseCase.command.condition()).isEqualTo(AssetCondition.DAMAGED);
        assertThat(changeAssetConditionUseCase.command.damageReport()).isEqualTo("Broken lens");
        assertThat(response.damageReport()).isEqualTo("Broken lens");
    }

    private static StaticAuthenticatedUserResolver authenticatedUserResolver(Role role) {
        return new StaticAuthenticatedUserResolver(new AuthenticatedUser(
                UUID.randomUUID(),
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

    private static class RecordingRegisterAssetUseCase implements RegisterAssetUseCase {
        private RegisterAssetCommand command;
        private AssetResult result;

        @Override
        public AssetResult register(RegisterAssetCommand command) {
            this.command = command;
            return result;
        }
    }

    private static class RecordingChangeAssetConditionUseCase implements ChangeAssetConditionUseCase {
        private ChangeAssetConditionCommand command;
        private AssetResult result;

        @Override
        public AssetResult changeCondition(ChangeAssetConditionCommand command) {
            this.command = command;
            return result;
        }
    }

    private static class EmptyFindAllAssetsUseCase implements FindAllAssetsUseCase {
        @Override
        public List<AssetResult> findAll() {
            return List.of();
        }
    }
}
