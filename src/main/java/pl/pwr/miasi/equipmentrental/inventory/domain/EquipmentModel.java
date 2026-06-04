package pl.pwr.miasi.equipmentrental.inventory.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.util.UUID;

public class EquipmentModel {

    private final UUID id;
    private final String name;
    private final String category;
    private final String manufacturer;

    public EquipmentModel(UUID id, String name, String category, String manufacturer) {
        if (id == null) {
            throw new BusinessException("Equipment model id cannot be null");
        }

        if (name == null || name.isBlank()) {
            throw new BusinessException("Equipment model name cannot be empty");
        }

        if (category == null || category.isBlank()) {
            throw new BusinessException("Equipment model category cannot be empty");
        }

        if (manufacturer == null || manufacturer.isBlank()) {
            throw new BusinessException("Equipment model manufacturer cannot be empty");
        }

        this.id = id;
        this.name = name;
        this.category = category;
        this.manufacturer = manufacturer;
    }

    public static EquipmentModel create(String name, String category, String manufacturer) {
        return new EquipmentModel(UUID.randomUUID(), name, category, manufacturer);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getManufacturer() {
        return manufacturer;
    }
}