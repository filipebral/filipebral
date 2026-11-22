ALTER TABLE app_users
    ALTER COLUMN email DROP NOT NULL;

ALTER TABLE app_users
    ADD COLUMN cpf VARCHAR(255),
    ADD COLUMN cpf_lookup_hash VARCHAR(64);

ALTER TABLE app_users
    ADD CONSTRAINT uk_app_users_cpf_lookup_hash UNIQUE (cpf_lookup_hash);
