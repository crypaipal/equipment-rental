package pl.pwr.miasi.equipmentrental.inventory.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "equipment_models")
public class EquipmentModelJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 100)
    private String manufacturer;

    protected EquipmentModelJpaEntity() {
    }

    public EquipmentModelJpaEntity(UUID id, String name, String category, String manufacturer) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.manufacturer = manufacturer;
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