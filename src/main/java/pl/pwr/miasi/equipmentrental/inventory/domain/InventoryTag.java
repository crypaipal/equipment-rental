package pl.pwr.miasi.equipmentrental.inventory.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

public record InventoryTag(String value) {

    public InventoryTag {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Inventory tag cannot be empty");
        }

        if (value.length() < 3) {
            throw new BusinessException("Inventory tag must have at least 3 characters");
        }
    }
}