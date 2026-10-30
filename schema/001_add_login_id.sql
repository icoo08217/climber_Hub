-- Existing members retain numeric PKs and all foreign-key relationships.
-- Run once before starting the session-authentication version of the app.
ALTER TABLE members ADD COLUMN login_id VARCHAR(50) NULL COMMENT '로그인 ID (소문자, 숫자, 밑줄)' AFTER member_id;
UPDATE members SET login_id = CONCAT('legacy_', member_id) WHERE login_id IS NULL;
ALTER TABLE members MODIFY COLUMN login_id VARCHAR(50) NOT NULL COMMENT '로그인 ID (소문자, 숫자, 밑줄)',
    ADD CONSTRAINT uk_members_login_id UNIQUE (login_id);
