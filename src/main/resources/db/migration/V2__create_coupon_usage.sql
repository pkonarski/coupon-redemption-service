CREATE TABLE IF NOT EXISTS coupons_usage (
    id UUID PRIMARY KEY,
    used_at TIMESTAMPTZ NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    coupon_id UUID REFERENCES coupons(id) NOT NULL,
    CONSTRAINT coupon_for_user UNIQUE (user_id, coupon_id)
)