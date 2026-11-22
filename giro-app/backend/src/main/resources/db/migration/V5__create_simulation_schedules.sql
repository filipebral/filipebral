CREATE TABLE simulation_schedules (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES simulation_accounts(user_id),
    request_id UUID NOT NULL,
    execution_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT uk_simulation_schedule_request UNIQUE (user_id, request_id)
);

CREATE INDEX idx_simulation_schedule_due ON simulation_schedules (status, scheduled_at);
