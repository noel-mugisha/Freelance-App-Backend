CREATE TABLE IF NOT EXISTS otp_tokens (
                                          id          BIGSERIAL PRIMARY KEY,
                                          token       VARCHAR(10) NOT NULL,
                                          user_id     BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                                          expiry_date TIMESTAMPTZ NOT NULL
);

-- Optional: Add an index for faster token lookups if needed, though user_id is unique.
CREATE INDEX IF NOT EXISTS idx_otp_tokens_user_id ON otp_tokens(user_id);