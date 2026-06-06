ALTER TABLE users
    ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0;

CREATE TABLE auth_sessions (
   token UUID PRIMARY KEY,
   user_id UUID NOT NULL,
   created_at TIMESTAMP NOT NULL,
   expires_at TIMESTAMP NOT NULL,

   CONSTRAINT fk_auth_sessions_user
       FOREIGN KEY (user_id)
           REFERENCES users(id)
);