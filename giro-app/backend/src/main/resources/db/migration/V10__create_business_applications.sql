CREATE TABLE business_applications (
    id UUID PRIMARY KEY,
    applicant_user_id UUID NOT NULL REFERENCES app_users(id),
    cnpj VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_business_application_user UNIQUE (applicant_user_id)
);
