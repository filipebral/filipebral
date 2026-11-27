ALTER TABLE audit_events ADD COLUMN exported_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE audit_events ADD COLUMN next_export_attempt_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_audit_events_export ON audit_events (exported_at, next_export_attempt_at, created_at);
