CREATE TABLE simulation_reconciliation (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES simulation_accounts(user_id),
    request_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    next_check_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_simulation_reconciliation_request UNIQUE (user_id, request_id)
);

CREATE INDEX idx_simulation_reconciliation_due ON simulation_reconciliation (status, next_check_at);
