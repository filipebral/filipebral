CREATE TABLE split_payment_shares (
    id UUID PRIMARY KEY,
    split_transaction_id UUID NOT NULL REFERENCES split_transactions(id),
    member_user_id UUID NOT NULL,
    allocated_amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    paid_at TIMESTAMP WITH TIME ZONE,
    paid_by UUID,
    CONSTRAINT uk_split_payment_member UNIQUE (split_transaction_id, member_user_id)
);

CREATE INDEX idx_split_payment_transaction ON split_payment_shares (split_transaction_id);
