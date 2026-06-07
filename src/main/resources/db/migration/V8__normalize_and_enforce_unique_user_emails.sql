UPDATE users
SET email = lower(trim(email));

DELETE FROM auth_sessions
WHERE user_id IN (
    SELECT id
    FROM (
             SELECT
                 id,
                 row_number() OVER (
                PARTITION BY lower(trim(email))
                ORDER BY id
            ) AS rn
             FROM users
         ) duplicated_users
    WHERE duplicated_users.rn > 1
);

DELETE FROM reservations
WHERE user_id IN (
    SELECT id
    FROM (
             SELECT
                 id,
                 row_number() OVER (
                PARTITION BY lower(trim(email))
                ORDER BY id
            ) AS rn
             FROM users
         ) duplicated_users
    WHERE duplicated_users.rn > 1
);

DELETE FROM rentals
WHERE user_id IN (
    SELECT id
    FROM (
             SELECT
                 id,
                 row_number() OVER (
                PARTITION BY lower(trim(email))
                ORDER BY id
            ) AS rn
             FROM users
         ) duplicated_users
    WHERE duplicated_users.rn > 1
);

DELETE FROM users
WHERE id IN (
    SELECT id
    FROM (
             SELECT
                 id,
                 row_number() OVER (
                PARTITION BY lower(trim(email))
                ORDER BY id
            ) AS rn
             FROM users
         ) duplicated_users
    WHERE duplicated_users.rn > 1
);

ALTER TABLE users
DROP CONSTRAINT IF EXISTS uk_users_email;

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_normalized
    ON users (lower(trim(email)));