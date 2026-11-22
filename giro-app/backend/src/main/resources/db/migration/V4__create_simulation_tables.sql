CREATE TABLE simulation_accounts (
    user_id UUID PRIMARY KEY REFERENCES app_users(id),
    balance NUMERIC(19, 2) NOT NULL
);

CREATE TABLE simulation_transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES simulation_accounts(user_id),
    request_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    description VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_simulation_request UNIQUE (user_id, request_id)
);

CREATE INDEX idx_simulation_transactions_user_created ON simulation_transactions (user_id, created_at DESC);
