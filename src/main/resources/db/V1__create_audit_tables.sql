-- =============================================
-- Chehar Temple - Audit Trail Tables
-- =============================================

-- 1. User Sessions (Login/Logout journey with device data)
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    device_id VARCHAR(255),
    device_name VARCHAR(255),
    device_model VARCHAR(255),
    os_name VARCHAR(100),
    os_version VARCHAR(50),
    app_version VARCHAR(50),
    ip_address VARCHAR(45),
    session_token VARCHAR(255) NOT NULL UNIQUE,
    login_source VARCHAR(20), -- ADMIN, USER, MOBILE
    login_at TIMESTAMP NOT NULL DEFAULT NOW(),
    logout_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_session_status CHECK (status IN ('ACTIVE', 'LOGGED_OUT', 'EXPIRED'))
);

CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_token ON user_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON user_sessions(status);
CREATE INDEX IF NOT EXISTS idx_sessions_login_at ON user_sessions(login_at DESC);

-- 2. Audit Logs (All actions across modules)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,   -- LOGIN, LOGOUT, SCREEN_VIEW, VIEW_LIVE_STREAM, OPEN_SOCIAL, etc.
    module VARCHAR(20) NOT NULL,   -- AUTH, ACTIVITY, EVENT, NEWS, GALLERY, TIMING, CONFIG, USER
    entity_id VARCHAR(50),
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_token VARCHAR(255),    -- Links to user_sessions.session_token for journey tracking
    source VARCHAR(20) NOT NULL,   -- ADMIN, USER, MOBILE
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_module ON audit_logs(module);
CREATE INDEX IF NOT EXISTS idx_audit_source ON audit_logs(source);
CREATE INDEX IF NOT EXISTS idx_audit_session_token ON audit_logs(session_token);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_logs(created_at DESC);
