package pl.pwr.miasi.equipmentrental.inventory.domain;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetTest {

    @Test
    void registeredAssetIsOperationalAndAvailableForRental() {
        Asset asset = Asset.register(UUID.randomUUID(), new InventoryTag("LAB-001"));

        assertThat(asset.getCondition()).isEqualTo(AssetCondition.OPERATIONAL);
        assertThat(asset.getDamageReport()).isNull();
        assertThat(asset.isAvailableForRental()).isTrue();
    }

    @Test
    void damagedAssetIsNotAvailableForRental() {
        Asset asset = Asset.register(UUID.randomUUID(), new InventoryTag("LAB-001"));

        asset.markAsDamaged("Broken lens mount");

        assertThat(asset.getCondition()).isEqualTo(AssetCondition.DAMAGED);
        assertThat(asset.getDamageReport()).isEqualTo("Broken lens mount");
        assertThat(asset.isAvailableForRental()).isFalse();
    }

    @Test
    void markingAssetAsDamagedRequiresDamageReport() {
        Asset asset = Asset.register(UUID.randomUUID(), new InventoryTag("LAB-001"));

        assertThatThrownBy(() -> asset.markAsDamaged(" "))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Damage report cannot be empty when marking asset as damaged");
    }
}
