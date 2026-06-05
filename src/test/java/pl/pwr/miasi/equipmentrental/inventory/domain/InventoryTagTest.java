package pl.pwr.miasi.equipmentrental.inventory.domain;

import org.junit.jupiter.api.Test;
import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryTagTest {

    @Test
    void createsInventoryTagForNonBlankValueWithAtLeastThreeCharacters() {
        InventoryTag inventoryTag = new InventoryTag("CAM-001");

        assertThat(inventoryTag.value()).isEqualTo("CAM-001");
    }

    @Test
    void rejectsTooShortInventoryTag() {
        assertThatThrownBy(() -> new InventoryTag("AB"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Inventory tag must have at least 3 characters");
    }
}
