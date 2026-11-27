CREATE TABLE simulation_cards (
    user_id UUID PRIMARY KEY REFERENCES simulation_accounts(user_id),
    credit_limit NUMERIC(19, 2) NOT NULL,
    blocked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE simulation_card_statements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES simulation_cards(user_id),
    statement_month VARCHAR(7) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_request_id UUID,
    CONSTRAINT uk_simulation_card_statement UNIQUE (user_id, statement_month)
);

CREATE TABLE simulation_card_transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES simulation_cards(user_id),
    request_id UUID NOT NULL,
    kind VARCHAR(10) NOT NULL,
    description VARCHAR(100) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    statement_month VARCHAR(7),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_simulation_card_request UNIQUE (user_id, request_id)
);

CREATE INDEX idx_simulation_card_tx_user_created ON simulation_card_transactions (user_id, created_at DESC);
