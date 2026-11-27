SELECT DATABASE() AS database_name;

SELECT column_name, column_type, is_nullable, column_default, column_key, column_comment
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'gym'
  AND column_name IN ('external_source', 'external_id', 'verification_status', 'active', 'verified_at')
ORDER BY ordinal_position;

SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_schema = DATABASE()
  AND table_name = 'gym'
  AND constraint_name IN (
    'uk_gym_external_source_id',
    'chk_gym_external_source',
    'chk_gym_verification_status'
  )
ORDER BY constraint_type, constraint_name;

SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns_in_index, non_unique
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'gym'
  AND index_name IN ('uk_gym_external_source_id', 'idx_gym_verification_active')
GROUP BY index_name, non_unique
ORDER BY index_name;

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
