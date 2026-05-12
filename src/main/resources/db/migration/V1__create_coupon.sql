CREATE TABLE IF NOT EXISTS coupons (
    id UUID PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    max_usages INTEGER NOT NULL,
    current_usages INTEGER NOT NULL DEFAULT 0,
    country_code VARCHAR(2) NOT NULL
);