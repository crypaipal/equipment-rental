CREATE TABLE assets (
    id UUID PRIMARY KEY,
    equipment_model_id UUID NOT NULL,
    inventory_tag VARCHAR(100) NOT NULL,
    condition VARCHAR(30) NOT NULL,
    damage_report VARCHAR(500),

    CONSTRAINT uk_assets_inventory_tag UNIQUE (inventory_tag),
    CONSTRAINT fk_assets_equipment_model
        FOREIGN KEY (equipment_model_id)
            REFERENCES equipment_models(id)
);