SELECT table_name, table_comment
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('comments', 'post_media')
ORDER BY table_name;

SELECT table_name, COUNT(*) AS documented_columns
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN ('comments', 'post_media')
  AND column_comment <> ''
GROUP BY table_name
ORDER BY table_name;

SELECT
  (SELECT COUNT(*) FROM comments) AS comments_count,
  (SELECT COUNT(*) FROM post_media) AS post_media_count;

SELECT
  COUNT(*) AS total_tables,
  SUM(table_comment <> '') AS documented_tables
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_type = 'BASE TABLE';

SELECT
  COUNT(*) AS total_columns,
  SUM(column_comment <> '') AS documented_columns
FROM information_schema.columns
WHERE table_schema = DATABASE();

SELECT table_name, column_name, column_type, column_comment
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'post'
  AND column_name = 'content';
