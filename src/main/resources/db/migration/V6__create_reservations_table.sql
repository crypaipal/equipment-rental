CREATE TABLE reservations (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  asset_id UUID NOT NULL,
  period_from TIMESTAMP NOT NULL,
  period_to TIMESTAMP NOT NULL,
  status VARCHAR(50) NOT NULL,
  rejection_reason VARCHAR(500),
  created_at TIMESTAMP NOT NULL,

  CONSTRAINT fk_reservations_user
      FOREIGN KEY (user_id)
          REFERENCES users(id),

  CONSTRAINT fk_reservations_asset
      FOREIGN KEY (asset_id)
          REFERENCES assets(id)
);