SELECT COUNT(*) AS member_count,
       COALESCE(SUM(CRC32(CONCAT_WS('|', member_id, COALESCE(login_id, ''), name, COALESCE(email, ''), COALESCE(password, ''), role))), 0) AS member_checksum
FROM members;
