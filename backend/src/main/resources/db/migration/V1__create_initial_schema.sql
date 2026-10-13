CREATE TABLE accounts (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    baas_account_id VARCHAR(255),
    routing_number VARCHAR(255),
    branch VARCHAR(255),
    account_number VARCHAR(255),
    available_balance NUMERIC(19, 4),
    status VARCHAR(255),
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uk_accounts_baas_account_id UNIQUE (baas_account_id)
);

CREATE TABLE split_groups (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_split_groups PRIMARY KEY (id)
);

CREATE TABLE group_members (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    percentage NUMERIC(5, 2) NOT NULL,
    group_id UUID,
    CONSTRAINT pk_group_members PRIMARY KEY (id),
    CONSTRAINT fk_group_members_split_group
        FOREIGN KEY (group_id) REFERENCES split_groups (id)
);

CREATE INDEX idx_group_members_group_id ON group_members (group_id);

CREATE TABLE split_transactions (
    id UUID NOT NULL,
    split_group_id UUID NOT NULL,
    description VARCHAR(255) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_split_transactions PRIMARY KEY (id)
);

CREATE INDEX idx_split_transactions_group_id ON split_transactions (split_group_id);
