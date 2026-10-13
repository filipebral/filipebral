CREATE TABLE app_users (
    id UUID NOT NULL,
    email VARCHAR(254) NOT NULL,
    name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    CONSTRAINT pk_app_users PRIMARY KEY (id),
    CONSTRAINT uk_app_users_email UNIQUE (email)
);

ALTER TABLE accounts
    ALTER COLUMN baas_account_id TYPE VARCHAR(1024);

ALTER TABLE accounts
    ALTER COLUMN routing_number TYPE VARCHAR(1024);

ALTER TABLE accounts
    ALTER COLUMN branch TYPE VARCHAR(1024);

ALTER TABLE accounts
    ALTER COLUMN account_number TYPE VARCHAR(1024);

ALTER TABLE split_groups
    ADD COLUMN owner_user_id UUID;
