CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    actor_user_id UUID NOT NULL REFERENCES app_users(id),
    event_type VARCHAR(50) NOT NULL,
    resource_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_audit_events_actor_created ON audit_events (actor_user_id, created_at DESC);
