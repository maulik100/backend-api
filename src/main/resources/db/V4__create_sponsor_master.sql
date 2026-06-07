-- =============================================
-- Chehar Temple - Sponsor Master Table
-- =============================================

CREATE TABLE IF NOT EXISTS sponsor_master (
    id                      BIGSERIAL PRIMARY KEY,
    title                   VARCHAR(255) NOT NULL,
    description             TEXT,
    media_type              VARCHAR(10) NOT NULL CHECK (media_type IN ('IMAGE', 'VIDEO')),
    media_link              TEXT NOT NULL,
    thumbnail_link          TEXT,
    display_start_date_time TIMESTAMP NOT NULL,
    display_end_date_time   TIMESTAMP NOT NULL,
    sponsor_status          VARCHAR(20) NOT NULL DEFAULT 'UPCOMING'
                                CHECK (sponsor_status IN ('UPCOMING','ACTIVE','INACTIVE','EXPIRED','CANCELLED')),
    priority_order          INTEGER NOT NULL DEFAULT 0,
    redirect_url            TEXT,
    click_count             BIGINT NOT NULL DEFAULT 0,
    display_sequence        INTEGER NOT NULL DEFAULT 0,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,
    remarks                 TEXT
);

CREATE INDEX IF NOT EXISTS idx_sponsor_status      ON sponsor_master(sponsor_status);
CREATE INDEX IF NOT EXISTS idx_sponsor_start_dt    ON sponsor_master(display_start_date_time);
CREATE INDEX IF NOT EXISTS idx_sponsor_end_dt      ON sponsor_master(display_end_date_time);
CREATE INDEX IF NOT EXISTS idx_sponsor_deleted     ON sponsor_master(deleted);
CREATE INDEX IF NOT EXISTS idx_sponsor_priority    ON sponsor_master(priority_order);
