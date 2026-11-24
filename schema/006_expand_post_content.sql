-- Align the post body column with the API's 10,000-character validation limit.
ALTER TABLE post
  MODIFY COLUMN content TEXT NOT NULL COMMENT '게시글 본문 (최대 10000자)';
