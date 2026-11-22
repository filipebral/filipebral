CREATE TABLE simulation_bills (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES simulation_accounts(user_id),
    request_id UUID NOT NULL,
    execution_id UUID NOT NULL,
    description VARCHAR(100) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT uk_simulation_bill_request UNIQUE (user_id, request_id)
);

CREATE INDEX idx_simulation_bills_user_due ON simulation_bills (user_id, due_date DESC);
