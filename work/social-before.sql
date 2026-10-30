SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() ORDER BY TABLE_NAME;
SELECT COUNT(*) AS existing_members FROM members;
SELECT member_id,SHA2(CAST(JSON_ARRAY(login_id,name,email,password,role,created_at,created_by,updated_at,updated_by) AS CHAR),256) AS row_digest FROM members ORDER BY member_id;
