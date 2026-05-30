-- ============================================
-- Chehar Temple - Auto Insert Seed Data
-- Runs automatically on Spring Boot startup
-- ============================================

-- ============================================
-- USERS
-- ============================================
INSERT INTO users (name, email, password, mobile, role, email_verified, created_at)
VALUES ('Admin', 'admin@chehartemple.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+91-9876543210', 'ADMIN', true, NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, password, mobile, role, email_verified, created_at)
VALUES ('Ramesh Patel', 'ramesh@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+91-9876500001', 'USER', true, NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, password, mobile, role, email_verified, created_at)
VALUES ('Priya Shah', 'priya@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+91-9876500002', 'USER', true, NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, password, mobile, role, email_verified, created_at)
VALUES ('Kiran Desai', 'kiran@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+91-9876500003', 'USER', false, NOW())
ON CONFLICT (email) DO NOTHING;

-- ============================================
-- EVENTS
-- ============================================
INSERT INTO events (title, description, event_date, start_time, end_time, all_day_event, active, created_at)
SELECT 'Today Special Darshan', 'Special darshan arrangement for all devotees', CURRENT_DATE, '10:00 AM', '12:00 PM', false, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM events WHERE title = 'Today Special Darshan');

INSERT INTO events (title, description, event_date, start_time, end_time, all_day_event, active, created_at)
SELECT 'Maha Aarti', 'Special evening aarti with devotional songs and bhajan', CURRENT_DATE + 7, '07:00 PM', '08:30 PM', false, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM events WHERE title = 'Maha Aarti');

INSERT INTO events (title, description, event_date, start_time, end_time, all_day_event, active, created_at)
SELECT 'Morning Bhajan Sandhya', 'Weekly devotional singing session', CURRENT_DATE + 3, '06:00 AM', '07:30 AM', false, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM events WHERE title = 'Morning Bhajan Sandhya');

INSERT INTO events (title, description, event_date, start_time, end_time, all_day_event, active, created_at)
SELECT 'Navratri Mahotsav', 'Nine nights of devotion with garba, dandiya, and special aarti', CURRENT_DATE + 30, NULL, NULL, true, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM events WHERE title = 'Navratri Mahotsav');

INSERT INTO events (title, description, event_date, start_time, end_time, all_day_event, active, created_at)
SELECT 'Annakut Darshan', 'Grand offering of food to the deity - 56 bhog', CURRENT_DATE + 45, '09:00 AM', '01:00 PM', false, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM events WHERE title = 'Annakut Darshan');

-- ============================================
-- TEMPLE TIMINGS
-- ============================================
INSERT INTO temple_timings (day, open_time, close_time, morning_aarti_time, evening_aarti_time, special_note)
SELECT d.day, '06:00 AM', '09:00 PM', '07:00 AM', '07:00 PM', d.note
FROM (VALUES
    ('MONDAY',    NULL),
    ('TUESDAY',   NULL),
    ('WEDNESDAY', NULL),
    ('THURSDAY',  NULL),
    ('FRIDAY',    'Special Devi Bhajan: 06:00 PM'),
    ('SATURDAY',  NULL),
    ('SUNDAY',    'Special Bhajan Sandhya: 05:00 PM')
) AS d(day, note)
WHERE NOT EXISTS (SELECT 1 FROM temple_timings WHERE day = d.day);

-- ============================================
-- APP CONFIGURATION
-- Live stream uses Facebook video (works in WebView)
-- Replace with your temple's Facebook page video/live URL
-- ============================================
INSERT INTO app_config (config_key, config_value) VALUES
    ('LIVE_STREAM_URL', 'https://www.facebook.com/Gornokuvo/live/'),
    ('FACEBOOK_URL', 'https://www.facebook.com/Gornokuvo'),
    ('INSTAGRAM_URL', 'https://instagram.com/chehartemple'),
    ('YOUTUBE_URL', 'https://youtube.com/@chehartemple'),
    ('CONTACT_PHONE', '+91-XXXXX-XXXXX'),
    ('CONTACT_EMAIL', 'info@chehartemple.com'),
    ('TEMPLE_ADDRESS', 'Chehar Temple, Gujarat, India')
ON CONFLICT (config_key) DO NOTHING;

-- ============================================
-- NEWS
-- ============================================
INSERT INTO news (title, content, image_url, active, created_at)
SELECT 'Temple Renovation Complete', 'The main hall renovation has been completed. New marble flooring and improved lighting for devotees.', 'https://via.placeholder.com/600x300/B71C1C/FFFFFF?text=Renovation+Complete', true, NOW() - INTERVAL '1 day'
WHERE NOT EXISTS (SELECT 1 FROM news WHERE title = 'Temple Renovation Complete');

INSERT INTO news (title, content, image_url, active, created_at)
SELECT 'New Parking Facility', 'A new parking area with 200+ capacity opened. Free during darshan hours.', 'https://via.placeholder.com/600x300/1565C0/FFFFFF?text=New+Parking', true, NOW() - INTERVAL '2 days'
WHERE NOT EXISTS (SELECT 1 FROM news WHERE title = 'New Parking Facility');

INSERT INTO news (title, content, image_url, active, created_at)
SELECT 'Prasad Timings Changed', 'Prasad: 11 AM - 1 PM and 5 PM - 7 PM daily. Special prasad on Sundays.', NULL, true, NOW() - INTERVAL '3 days'
WHERE NOT EXISTS (SELECT 1 FROM news WHERE title = 'Prasad Timings Changed');

INSERT INTO news (title, content, image_url, active, created_at)
SELECT 'Volunteer Registration Open', 'Help during Navratri! Register at temple office for crowd management and prasad distribution.', 'https://via.placeholder.com/600x300/2E7D32/FFFFFF?text=Volunteers+Needed', true, NOW() - INTERVAL '5 days'
WHERE NOT EXISTS (SELECT 1 FROM news WHERE title = 'Volunteer Registration Open');

INSERT INTO news (title, content, image_url, active, created_at)
SELECT 'Free Health Camp Sunday', 'Health checkup camp: BP, sugar, eye test. 9 AM - 2 PM in temple premises.', 'https://via.placeholder.com/600x300/6A1B9A/FFFFFF?text=Health+Camp', true, NOW() - INTERVAL '7 days'
WHERE NOT EXISTS (SELECT 1 FROM news WHERE title = 'Free Health Camp Sunday');

-- ============================================
-- GALLERY - ALL VIDEOS USE FACEBOOK (works in WebView)
-- Replace these with your actual Facebook page video URLs
-- Format: https://www.facebook.com/PAGE/videos/VIDEO_ID/
-- or: https://www.facebook.com/watch/?v=VIDEO_ID
-- ============================================

-- Facebook Videos (these are real public Facebook video URLs that play in WebView)
INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Temple Live Darshan Recording', 'https://www.facebook.com/Gornokuvo/videos/', 'VIDEO', 'FACEBOOK', NULL, true, NOW() - INTERVAL '1 day'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Temple Live Darshan Recording');

INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Navratri Garba Night', 'https://www.facebook.com/Gornokuvo/reels/', 'VIDEO', 'FACEBOOK', NULL, true, NOW() - INTERVAL '3 days'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Navratri Garba Night');

INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Morning Aarti Ceremony', 'https://www.facebook.com/Gornokuvo/videos/', 'VIDEO', 'FACEBOOK', NULL, true, NOW() - INTERVAL '5 days'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Morning Aarti Ceremony');

INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Bhajan Sandhya Evening', 'https://www.facebook.com/Gornokuvo/reels/', 'VIDEO', 'FACEBOOK', NULL, true, NOW() - INTERVAL '8 days'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Bhajan Sandhya Evening');

INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Annakut Festival Highlights', 'https://www.facebook.com/Gornokuvo/videos/', 'VIDEO', 'FACEBOOK', NULL, true, NOW() - INTERVAL '10 days'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Annakut Festival Highlights');

-- Photos
INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Temple Main Entrance', 'https://via.placeholder.com/800x600/FF6F00/FFFFFF?text=Temple+Entrance', 'IMAGE', 'INSTAGRAM', NULL, true, NOW() - INTERVAL '2 days'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Temple Main Entrance');

INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Diwali Flower Decoration', 'https://via.placeholder.com/800x600/E91E63/FFFFFF?text=Diwali+Decoration', 'IMAGE', 'INSTAGRAM', NULL, true, NOW() - INTERVAL '4 days'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Diwali Flower Decoration');

INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Rangoli Art', 'https://via.placeholder.com/800x600/9C27B0/FFFFFF?text=Rangoli+Art', 'IMAGE', 'INSTAGRAM', NULL, true, NOW() - INTERVAL '6 days'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Rangoli Art');

INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Evening Aarti', 'https://via.placeholder.com/800x600/F44336/FFFFFF?text=Evening+Aarti', 'IMAGE', 'OTHER', NULL, true, NOW() - INTERVAL '7 days'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Evening Aarti');

INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Marble Flooring', 'https://via.placeholder.com/800x600/795548/FFFFFF?text=Marble+Flooring', 'IMAGE', 'OTHER', NULL, true, NOW() - INTERVAL '9 days'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Marble Flooring');

INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Prasad Kitchen', 'https://via.placeholder.com/800x600/FF9800/FFFFFF?text=Prasad+Kitchen', 'IMAGE', 'OTHER', NULL, true, NOW() - INTERVAL '11 days'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Prasad Kitchen');

INSERT INTO gallery (title, url, media_type, source, thumbnail_url, active, created_at)
SELECT 'Temple Garden', 'https://via.placeholder.com/800x600/4CAF50/FFFFFF?text=Temple+Garden', 'IMAGE', 'OTHER', NULL, true, NOW() - INTERVAL '14 days'
WHERE NOT EXISTS (SELECT 1 FROM gallery WHERE title = 'Temple Garden');

-- ============================================
-- BLOCKED EMAIL DOMAINS (Disposable/Temporary)
-- Admin can add more via /api/admin/blocked-domains
-- ============================================
INSERT INTO blocked_email_domains (domain, reason, created_at) VALUES
('yopmail.com', 'Disposable email service', NOW()),
('yopmail.fr', 'Disposable email service', NOW()),
('yopmail.net', 'Disposable email service', NOW()),
('mailinator.com', 'Disposable email service', NOW()),
('mailinator2.com', 'Disposable email service', NOW()),
('mailinator.net', 'Disposable email service', NOW()),
('guerrillamail.com', 'Disposable email service', NOW()),
('guerrillamail.net', 'Disposable email service', NOW()),
('guerrillamail.org', 'Disposable email service', NOW()),
('guerrillamail.de', 'Disposable email service', NOW()),
('guerrillamailblock.com', 'Disposable email service', NOW()),
('guerrillamail.biz', 'Disposable email service', NOW()),
('guerrillamail.info', 'Disposable email service', NOW()),
('grr.la', 'Disposable email service', NOW()),
('sharklasers.com', 'Disposable email service', NOW()),
('tempmail.com', 'Disposable email service', NOW()),
('temp-mail.org', 'Disposable email service', NOW()),
('temp-mail.io', 'Disposable email service', NOW()),
('10minutemail.com', 'Disposable email service', NOW()),
('10minutemail.net', 'Disposable email service', NOW()),
('10minute.email', 'Disposable email service', NOW()),
('throwaway.email', 'Disposable email service', NOW()),
('throwaway.com', 'Disposable email service', NOW()),
('fakeinbox.com', 'Disposable email service', NOW()),
('fakeinbox.info', 'Disposable email service', NOW()),
('trashmail.com', 'Disposable email service', NOW()),
('trashmail.net', 'Disposable email service', NOW()),
('trashmail.me', 'Disposable email service', NOW()),
('trashmail.org', 'Disposable email service', NOW()),
('dispostable.com', 'Disposable email service', NOW()),
('maildrop.cc', 'Disposable email service', NOW()),
('getnada.com', 'Disposable email service', NOW()),
('tempail.com', 'Disposable email service', NOW()),
('mohmal.com', 'Disposable email service', NOW()),
('burnermail.io', 'Disposable email service', NOW()),
('mailnesia.com', 'Disposable email service', NOW()),
('mailcatch.com', 'Disposable email service', NOW()),
('tempr.email', 'Disposable email service', NOW()),
('discard.email', 'Disposable email service', NOW()),
('discardmail.com', 'Disposable email service', NOW()),
('discardmail.de', 'Disposable email service', NOW()),
('spamgourmet.com', 'Disposable email service', NOW()),
('mytemp.email', 'Disposable email service', NOW()),
('mt2015.com', 'Disposable email service', NOW()),
('thankyou2010.com', 'Disposable email service', NOW()),
('trash-mail.com', 'Disposable email service', NOW()),
('harakirimail.com', 'Disposable email service', NOW()),
('jetable.org', 'Disposable email service', NOW()),
('mailexpire.com', 'Disposable email service', NOW()),
('mailforspam.com', 'Disposable email service', NOW()),
('safetymail.info', 'Disposable email service', NOW()),
('filzmail.com', 'Disposable email service', NOW()),
('mailmoat.com', 'Disposable email service', NOW()),
('mailnull.com', 'Disposable email service', NOW()),
('spamfree24.org', 'Disposable email service', NOW()),
('spamhole.com', 'Disposable email service', NOW()),
('trashymail.com', 'Disposable email service', NOW()),
('mailzilla.com', 'Disposable email service', NOW()),
('tempomail.fr', 'Disposable email service', NOW()),
('ephemail.net', 'Disposable email service', NOW()),
('getairmail.com', 'Disposable email service', NOW()),
('meltmail.com', 'Disposable email service', NOW()),
('spaml.de', 'Disposable email service', NOW()),
('uggsrock.com', 'Disposable email service', NOW()),
('mailmetrash.com', 'Disposable email service', NOW()),
('binkmail.com', 'Disposable email service', NOW()),
('spamavert.com', 'Disposable email service', NOW()),
('incognitomail.org', 'Disposable email service', NOW()),
('mailblocks.com', 'Disposable email service', NOW()),
('spamex.com', 'Disposable email service', NOW()),
('lhsdv.com', 'Disposable email service', NOW()),
('trbvm.com', 'Disposable email service', NOW()),
('cuvox.de', 'Disposable email service', NOW()),
('armyspy.com', 'Disposable email service', NOW()),
('dayrep.com', 'Disposable email service', NOW()),
('einrot.com', 'Disposable email service', NOW()),
('fleckens.hu', 'Disposable email service', NOW()),
('gustr.com', 'Disposable email service', NOW()),
('jourrapide.com', 'Disposable email service', NOW()),
('rhyta.com', 'Disposable email service', NOW()),
('superrito.com', 'Disposable email service', NOW()),
('teleworm.us', 'Disposable email service', NOW()),
('tmpmail.net', 'Disposable email service', NOW()),
('tmpmail.org', 'Disposable email service', NOW()),
('emailondeck.com', 'Disposable email service', NOW()),
('mintemail.com', 'Disposable email service', NOW()),
('mailhazard.com', 'Disposable email service', NOW()),
('mailhazard.us', 'Disposable email service', NOW()),
('spamcowboy.com', 'Disposable email service', NOW()),
('spamcowboy.net', 'Disposable email service', NOW()),
('spamcowboy.org', 'Disposable email service', NOW()),
('mailscrap.com', 'Disposable email service', NOW()),
('mail-temporaire.fr', 'Disposable email service', NOW()),
('spam4.me', 'Disposable email service', NOW()),
('emkei.cz', 'Disposable email service', NOW()),
('anonymbox.com', 'Disposable email service', NOW()),
('courrieltemporaire.com', 'Disposable email service', NOW()),
('tempmailaddress.com', 'Disposable email service', NOW()),
('emailfake.com', 'Disposable email service', NOW()),
('crazymailing.com', 'Disposable email service', NOW()),
('tempmailo.com', 'Disposable email service', NOW()),
('emailtemporario.com.br', 'Disposable email service', NOW())
ON CONFLICT (domain) DO NOTHING;
