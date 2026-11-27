SELECT DATABASE() AS database_name;

SELECT COUNT(*) AS gym_row_count FROM gym;

SELECT COUNT(*) AS new_column_count
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'gym'
  AND column_name IN ('external_source', 'external_id', 'verification_status', 'active', 'verified_at');

SELECT 'comments' AS table_name, COUNT(*) AS row_count FROM comments
UNION ALL SELECT 'gym', COUNT(*) FROM gym
UNION ALL SELECT 'gym_staff', COUNT(*) FROM gym_staff
UNION ALL SELECT 'members', COUNT(*) FROM members
UNION ALL SELECT 'post', COUNT(*) FROM post
UNION ALL SELECT 'post_media', COUNT(*) FROM post_media
UNION ALL SELECT 'reservation', COUNT(*) FROM reservation
UNION ALL SELECT 'social_accounts', COUNT(*) FROM social_accounts
UNION ALL SELECT 'timeslot', COUNT(*) FROM timeslot;

CHECKSUM TABLE comments, gym, gym_staff, members, post, post_media, reservation, social_accounts, timeslot;
