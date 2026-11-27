SELECT COUNT(*) AS total_gyms,
       SUM(verification_status = 'VERIFIED') AS verified_gyms,
       SUM(verification_status = 'PENDING') AS pending_gyms,
       SUM(active = b'1') AS active_gyms,
       SUM(phone IS NOT NULL) AS gyms_with_phone,
       SUM(open_hours IS NOT NULL) AS gyms_with_open_hours,
       SUM(pricing_info IS NOT NULL) AS gyms_with_pricing
FROM gym;

SELECT external_source, verification_status, active, COUNT(*) AS gym_count
FROM gym
GROUP BY external_source, verification_status, active
ORDER BY external_source, verification_status, active;

SELECT COUNT(DISTINCT external_id) AS distinct_external_ids,
       COUNT(DISTINCT address) AS distinct_addresses,
       MIN(latitude) AS min_latitude,
       MAX(latitude) AS max_latitude,
       MIN(longitude) AS min_longitude,
       MAX(longitude) AS max_longitude
FROM gym;

SELECT name, address, verification_status, active
FROM gym
WHERE verification_status = 'PENDING'
ORDER BY name;

SELECT name, address, phone, external_id, verification_status
FROM gym
WHERE name IN (
  '피커스 종로점', '피커스클라이밍 구로점', '피커스 신촌점',
  '서울숲클라이밍 구로점', '서울숲클라이밍 종로점', '서울숲클라이밍 잠실점', '서울숲클라이밍 영등포점',
  '더클라임 신림점', '더클라임 연남점', '더클라임 성수점', '더클라임 문래점', '더클라임 이수점',
  '더클라임 양재점', '더클라임 사당점', '더클라임 마곡점', '더클라임 논현점', '더클라임 강남점',
  '온플릭클라이밍 천호점', '오프더월클라이밍'
)
ORDER BY name;

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
