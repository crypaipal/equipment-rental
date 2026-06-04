CREATE TABLE equipment_models (
  id           UUID PRIMARY KEY,
  name         VARCHAR(100) NOT NULL,
  category     VARCHAR(100) NOT NULL,
  manufacturer VARCHAR(100) NOT NULL,

  CONSTRAINT uk_equipment_model_name_manufacturer UNIQUE (name, manufacturer)
);