CREATE TABLE rentals (
    id UUID PRIMARY KEY,
    reservation_id UUID NOT NULL,
    user_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    checkout_at TIMESTAMP NOT NULL,
    expected_return_at TIMESTAMP NOT NULL,
    returned_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,

    CONSTRAINT fk_rentals_reservation
     FOREIGN KEY (reservation_id)
         REFERENCES reservations(id),

    CONSTRAINT fk_rentals_user
     FOREIGN KEY (user_id)
         REFERENCES users(id),

    CONSTRAINT fk_rentals_asset
     FOREIGN KEY (asset_id)
         REFERENCES assets(id)
);