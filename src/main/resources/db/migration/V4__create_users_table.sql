CREATE TABLE users (
   id UUID PRIMARY KEY,
   first_name VARCHAR(100) NOT NULL,
   last_name VARCHAR(100) NOT NULL,
   email VARCHAR(150) NOT NULL,
   password_hash VARCHAR(255) NOT NULL,
   role VARCHAR(50) NOT NULL,
   locked_until TIMESTAMP,
   lock_reason VARCHAR(500),

   CONSTRAINT uk_users_email UNIQUE (email)
);