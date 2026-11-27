SELECT DATABASE() AS database_name;

SELECT table_name, table_comment
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_name = 'gym_staff';

SELECT column_name, column_type, is_nullable, column_key, column_comment
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'gym_staff'
ORDER BY ordinal_position;

SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_schema = DATABASE() AND table_name = 'gym_staff'
ORDER BY constraint_type, constraint_name;

SELECT 'comments' AS table_name, COUNT(*) AS row_count FROM comments
UNION ALL SELECT 'gym', COUNT(*) FROM gym
UNION ALL SELECT 'gym_staff', COUNT(*) FROM gym_staff
UNION ALL SELECT 'members', COUNT(*) FROM members
UNION ALL SELECT 'post', COUNT(*) FROM post
UNION ALL SELECT 'post_media', COUNT(*) FROM post_media
UNION ALL SELECT 'reservation', COUNT(*) FROM reservation
UNION ALL SELECT 'social_accounts', COUNT(*) FROM social_accounts
UNION ALL SELECT 'timeslot', COUNT(*) FROM timeslot;

CHECKSUM TABLE comments, gym, members, post, post_media, reservation, social_accounts, timeslot;
