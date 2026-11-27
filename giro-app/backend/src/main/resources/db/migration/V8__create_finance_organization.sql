ALTER TABLE simulation_accounts
    ADD COLUMN reserved_balance NUMERIC(19, 2) NOT NULL DEFAULT 0;

ALTER TABLE simulation_transactions
    ADD COLUMN category VARCHAR(30);

ALTER TABLE simulation_card_transactions
    ADD COLUMN category VARCHAR(30);

CREATE TABLE simulation_budgets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES simulation_accounts(user_id),
    budget_month VARCHAR(7) NOT NULL,
    category VARCHAR(30) NOT NULL,
    limit_amount NUMERIC(19, 2) NOT NULL,
    CONSTRAINT uk_simulation_budget UNIQUE (user_id, budget_month, category)
);

CREATE TABLE simulation_goals (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES simulation_accounts(user_id),
    name VARCHAR(80) NOT NULL,
    target_amount NUMERIC(19, 2) NOT NULL,
    saved_amount NUMERIC(19, 2) NOT NULL
);

CREATE INDEX idx_simulation_goals_user ON simulation_goals (user_id);

CREATE TABLE simulation_goal_movements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES simulation_accounts(user_id),
    goal_id UUID NOT NULL REFERENCES simulation_goals(id),
    request_id UUID NOT NULL,
    type VARCHAR(10) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    CONSTRAINT uk_simulation_goal_request UNIQUE (user_id, request_id)
);

CREATE TABLE in_app_notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users(id),
    event_key VARCHAR(120) NOT NULL,
    type VARCHAR(30) NOT NULL,
    message VARCHAR(160) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_in_app_notification_event UNIQUE (user_id, event_key)
);

CREATE INDEX idx_in_app_notifications_user_created ON in_app_notifications (user_id, created_at DESC);
