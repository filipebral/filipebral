ALTER TABLE audit_events ALTER COLUMN actor_user_id DROP NOT NULL;
ALTER TABLE audit_events ALTER COLUMN resource_id DROP NOT NULL;

CREATE INDEX idx_audit_events_type_created ON audit_events (event_type, created_at DESC);
