-- =============================================
-- Chehar Temple - Security Enhancement Migration
-- Add account lockout fields to users table
-- =============================================

-- Add failed login attempts tracking
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INT DEFAULT 0;

-- Add account lock timestamp
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_locked_until TIMESTAMP;

-- Index for finding locked accounts (useful for admin dashboard)
CREATE INDEX IF NOT EXISTS idx_users_account_locked ON users(account_locked_until) WHERE account_locked_until IS NOT NULL;
