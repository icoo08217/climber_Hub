SELECT COUNT(*) AS member_count,
       COALESCE(SUM(CRC32(CONCAT_WS('|', member_id, COALESCE(login_id, ''), name, COALESCE(email, ''), COALESCE(password, ''), role))), 0) AS member_checksum,
       SUM(profile_image_url IS NOT NULL) AS profile_image_count
FROM members;

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'members'
  AND COLUMN_NAME IN ('profile_image_url', 'profile_image_public_id')
ORDER BY ORDINAL_POSITION;
