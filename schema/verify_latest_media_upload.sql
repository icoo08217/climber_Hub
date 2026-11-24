SELECT
  p.post_id,
  p.title,
  pm.media_id,
  pm.media_type,
  pm.original_name,
  pm.file_size,
  SUBSTRING_INDEX(SUBSTRING_INDEX(pm.media_url, '/', 3), '/', -1) AS media_host,
  (pm.public_id IS NOT NULL AND pm.public_id <> '') AS has_public_id,
  pm.created_at
FROM post_media pm
JOIN post p ON p.post_id = pm.post_id
ORDER BY pm.media_id DESC
LIMIT 5;

SELECT COUNT(*) AS total_media,
       SUM(media_type = 'IMAGE') AS image_count,
       SUM(media_type = 'VIDEO') AS video_count
FROM post_media;
